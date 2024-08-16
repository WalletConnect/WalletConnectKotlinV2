package com.walletconnect.sample.dapp.ui.routes.composable_routes.chain_selection

import android.content.Context
import android.widget.Toast
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import java.net.URLEncoder
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.core.net.toUri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.android.utils.isPackageInstalled
import com.walletconnect.sample.common.Chains
import com.walletconnect.sample.common.CompletePreviews
import com.walletconnect.sample.common.ui.WCTopAppBarLegacy
import com.walletconnect.sample.common.ui.coloredShadow
import com.walletconnect.sample.common.ui.commons.BlueButton
import com.walletconnect.sample.common.ui.conditionalModifier
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.sample.common.ui.toColor
import com.walletconnect.sample.dapp.BuildConfig
import com.walletconnect.sample.dapp.ui.DappSampleEvents
import com.walletconnect.sample.dapp.ui.routes.Route
import com.walletconnect.sign.client.Sign
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.wcmodal.ui.openWalletConnectModal
import com.walletconnect.wcmodal.ui.state.rememberModalState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ChainSelectionRoute(navController: NavController, dispatcher: CoroutineDispatcher = Dispatchers.Main) {
    val context = LocalContext.current
    val composableScope = rememberCoroutineScope()
    val viewModel: ChainSelectionViewModel = viewModel()
    val chainsState by viewModel.uiState.collectAsState()
    rememberModalState(navController = navController)
    val awaitingProposalResponse = viewModel.awaitingSharedFlow.collectAsState(false).value
    var pairingUri by remember { mutableStateOf(PairingUri(uri = "", isReCaps = false)) }

    handleSignEvents(viewModel, navController, context) { pairingUri = PairingUri(uri = "", isReCaps = false) }
    ChainSelectionScreen(
        composableScope,
        dispatcher,
        chains = chainsState,
        awaitingState = awaitingProposalResponse,
        pairingUri = pairingUri,
        context,
        onDialogDismiss = { pairingUri = PairingUri(uri = "", isReCaps = false) },
        onChainClick = viewModel::updateChainSelectState,
        onConnectClick = { onConnectClick(viewModel, navController, context) },
        onAuthenticateClick = { onAuthenticate(viewModel, composableScope, dispatcher, context) { uri -> pairingUri = uri } },
        onAuthenticateLinkMode = { appLink -> onAuthenticateLinkMode(viewModel, appLink, context, composableScope, dispatcher) },
        onAuthenticateSIWEClick = { onAuthenticateSIWE(viewModel, composableScope, dispatcher, context) { uri -> pairingUri = uri } }
    )
}

private fun onAuthenticateSIWE(
    viewModel: ChainSelectionViewModel,
    composableScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    context: Context,
    onSuccess: (PairingUri) -> Unit,
) {
    if (viewModel.isAnyChainSelected) {
        viewModel.authenticate(
            viewModel.siweParams,
            onAuthenticateSuccess = { uri -> onSuccess(PairingUri(uri ?: "", false)) },
            onError = { error ->
                composableScope.launch(dispatcher) {
                    Toast.makeText(context, "Authenticate error: $error", Toast.LENGTH_SHORT).show()
                }
            })
    } else {
        composableScope.launch(dispatcher) {
            Toast.makeText(context, "Please select a chain", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun onAuthenticateLinkMode(
    viewModel: ChainSelectionViewModel,
    appLink: String,
    context: Context,
    composableScope: CoroutineScope,
    dispatcher: CoroutineDispatcher
) {
    if (viewModel.isAnyChainSelected) {
        viewModel.authenticate(
            viewModel.authenticateParams,
            appLink,
            onAuthenticateSuccess = { uri -> onAuthenticateSuccess(uri, appLink, context, composableScope, dispatcher) },
            onError = { error ->
                composableScope.launch(dispatcher) {
                    Toast.makeText(context, "Authenticate error: $error", Toast.LENGTH_SHORT).show()
                }
            })
    } else {
        composableScope.launch(dispatcher) {
            Toast.makeText(context, "Please select a chain", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun onAuthenticateSuccess(
    uri: String?,
    appLink: String,
    context: Context,
    composableScope: CoroutineScope,
    dispatcher: CoroutineDispatcher
) {
    if (uri != null) {
        if (appLink.contains("walletkit_rn")) {
            redirectToRNWallet(uri, context, composableScope, dispatcher)
        } else {
            redirectToKotlinWallet(uri, context, composableScope, dispatcher)
        }
    }
}

private fun redirectToKotlinWallet(uri: String?, context: Context, composableScope: CoroutineScope, dispatcher: CoroutineDispatcher) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val encoded = URLEncoder.encode(uri, "UTF-8")
            data = "kotlin-web3wallet://wc?uri=$encoded".toUri()
            `package` = when (BuildConfig.BUILD_TYPE) {
                "debug" -> SAMPLE_WALLET_DEBUG_PACKAGE
                "internal" -> SAMPLE_WALLET_INTERNAL_PACKAGE
                else -> SAMPLE_WALLET_RELEASE_PACKAGE
            }
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        composableScope.launch(dispatcher) {
            Toast.makeText(context, "Please install Kotlin Sample Wallet", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun redirectToRNWallet(uri: String?, context: Context, composableScope: CoroutineScope, dispatcher: CoroutineDispatcher) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val encoded = URLEncoder.encode(uri, "UTF-8")
            data = "rn-web3wallet://wc?uri=$encoded".toUri()
            `package` = "com.walletconnect.web3wallet.rnsample.internal"
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        composableScope.launch(dispatcher) {
            Toast.makeText(context, "Please install RN Wallet", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun onAuthenticate(
    viewModel: ChainSelectionViewModel,
    composableScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    context: Context,
    onSuccess: (PairingUri) -> Unit,
) {
    if (viewModel.isAnyChainSelected) {
        viewModel.authenticate(
            viewModel.authenticateParams,
            onAuthenticateSuccess = { uri -> onSuccess(PairingUri(uri ?: "", true)) },
            onError = { error ->
                composableScope.launch(dispatcher) {
                    Toast.makeText(context, "Authenticate error: $error", Toast.LENGTH_SHORT).show()
                }
            })
    } else {
        composableScope.launch(dispatcher) {
            Toast.makeText(context, "Please select a chain", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun ChainSelectionScreen(
    composableScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    chains: List<ChainSelectionUi>,
    awaitingState: Boolean,
    pairingUri: PairingUri,
    context: Context,
    onDialogDismiss: () -> Unit,
    onChainClick: (Int, Boolean) -> Unit,
    onConnectClick: () -> Unit,
    onAuthenticateLinkMode: (String) -> Unit,
    onAuthenticateClick: () -> Unit,
    onAuthenticateSIWEClick: () -> Unit
) {
    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            WCTopAppBarLegacy(titleText = "Chain selection")
            ChainsList(
                chains = chains,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                onChainClick,
            )
            BlueButton(
                text = "Connect via WalletConnect Modal",
                onClick = onConnectClick,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp),
            )
            BlueButton(
                text = "1-CA Link Mode (Kotlin Sample Wallet)",
                onClick = { onAuthenticateLinkMode("https://web3modal-laboratory-git-chore-kotlin-assetlinks-walletconnect1.vercel.app/wallet") },
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            )
            BlueButton(
                text = "1-CA Link Mode (RN Wallet)",
                onClick = { onAuthenticateLinkMode("https://lab.web3modal.com/walletkit_rn") },
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            )

            BlueButton(
                text = "1-CA",
                onClick = onAuthenticateClick,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            )
            BlueButton(
                text = "1-CA (SIWE)",
                onClick = onAuthenticateSIWEClick,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            )
        }
        if (awaitingState) {
            Loader()
        }

        if (pairingUri.uri.isNotEmpty()) {
            QRDialog(composableScope, dispatcher, pairingUri, onDismissRequest = { onDialogDismiss() }, context)
        }
    }
}

@Composable
private fun QRDialog(composableScope: CoroutineScope, dispatcher: CoroutineDispatcher, pairingUri: PairingUri, onDismissRequest: () -> Unit, context: Context) {
    val qrBitmap = generateQRCode(pairingUri.uri)
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Box(
            modifier = Modifier
                .size(600.dp)
                .background(color = themedColor(Color(0xFF242425), Color(0xFFFFFFFF)), shape = MaterialTheme.shapes.medium)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp)
                    )
                } ?: Text("Error while generating QR code", modifier = Modifier.padding(16.dp))
                Button(
                    onClick = { onKotlinWalletDeepLink(onDismissRequest, pairingUri, context, composableScope, dispatcher) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Deep link to Kotlin Wallet")
                }
                if (pairingUri.isReCaps) {
                    showReCapsButton(onDismissRequest, pairingUri, context, composableScope, dispatcher)
                }
                Button(
                    onClick = {
                        composableScope.launch(dispatcher) {
                            Toast.makeText(context, "URI copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        clipboardManager.setText(AnnotatedString(pairingUri.uri))
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Copy URI to clipboard")
                }
                Button(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

private fun onKotlinWalletDeepLink(
    onDismissRequest: () -> Unit,
    pairingUri: PairingUri,
    context: Context,
    composableScope: CoroutineScope,
    dispatcher: CoroutineDispatcher
) {
    onDismissRequest()
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val encoded = URLEncoder.encode(pairingUri.uri, "UTF-8")
            data = "kotlin-web3wallet://wc?uri=$encoded".toUri()
            `package` = when (BuildConfig.BUILD_TYPE) {
                "debug" -> SAMPLE_WALLET_DEBUG_PACKAGE
                "internal" -> SAMPLE_WALLET_INTERNAL_PACKAGE
                else -> SAMPLE_WALLET_RELEASE_PACKAGE
            }
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        composableScope.launch(dispatcher) {
            Toast.makeText(context, "Please install Kotlin Sample Wallet", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun showReCapsButton(
    onDismissRequest: () -> Unit,
    pairingUri: PairingUri,
    context: Context,
    composableScope: CoroutineScope,
    dispatcher: CoroutineDispatcher
) {
    Button(
        onClick = { onDynamicSwitcher(onDismissRequest, pairingUri, context, composableScope, dispatcher) },
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text("Dynamic Switcher Deeplink (TrustWallet)", textAlign = TextAlign.Center)
    }
}

private fun onDynamicSwitcher(
    onDismissRequest: () -> Unit,
    pairingUri: PairingUri,
    context: Context,
    composableScope: CoroutineScope,
    dispatcher: CoroutineDispatcher
) {
    onDismissRequest()
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val encoded = URLEncoder.encode(pairingUri.uri, "UTF-8")
            data = "trust://wc?uri=$encoded".toUri()
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        composableScope.launch(dispatcher) {
            Toast.makeText(context, "Please install TrustWallet", Toast.LENGTH_SHORT).show()
        }
    }
}

fun generateQRCode(content: String): Bitmap? {
    val qrgEncoder = QRGEncoder(content, null, QRGContents.Type.TEXT, 400)
    return try {
        qrgEncoder.bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
private fun handleSignEvents(
    viewModel: ChainSelectionViewModel,
    navController: NavController,
    context: Context,
    onAuthenticateReject: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.walletEvents.collect { event ->
            when (event) {
                DappSampleEvents.SessionApproved -> {
                    viewModel.awaitingProposalResponse(false)
                    navController.navigate(Route.Session.path)
                }

                DappSampleEvents.SessionRejected -> {
                    viewModel.awaitingProposalResponse(false)
                    Toast.makeText(context, "Session has been rejected", Toast.LENGTH_SHORT).show()
                }

                DappSampleEvents.ProposalExpired -> {
                    viewModel.awaitingProposalResponse(false)
                    Toast.makeText(context, "Proposal has been expired", Toast.LENGTH_SHORT).show()
                }

                is DappSampleEvents.SessionAuthenticateApproved -> {
                    viewModel.awaitingProposalResponse(false)
                    if (event.message != null) {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate(Route.Session.path)
                    }
                }

                DappSampleEvents.SessionAuthenticateRejected -> {
                    viewModel.awaitingProposalResponse(false)
                    onAuthenticateReject()
                    Toast.makeText(context, "Session authenticate has been rejected", Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }
}

private fun onConnectClick(
    viewModel: ChainSelectionViewModel,
    navController: NavController,
    context: Context
) {
    if (viewModel.isAnyChainSelected) {
        WalletConnectModal.setSessionParams(viewModel.getSessionParams())
        navController.openWalletConnectModal()
    } else {
        Toast.makeText(context, "Please select a chain", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun BoxScope.Loader() {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .clip(RoundedCornerShape(34.dp))
            .background(themedColor(Color(0xFF242425).copy(alpha = .95f), Color(0xFFF2F2F7).copy(alpha = .95f)))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            strokeWidth = 8.dp,
            modifier = Modifier
                .size(75.dp), color = Color(0xFFB8F53D)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Awaiting response...",
            maxLines = 1,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                color = themedColor(Color(0xFFb9b3b5), Color(0xFF484648))
            ),
        )
    }
}

@Composable
private fun ChainsList(
    chains: List<ChainSelectionUi>,
    modifier: Modifier,
    onChainClick: (Int, Boolean) -> Unit
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(chains) { index, chain ->
            ChainItem(
                index = index,
                chain = chain,
                onChainClick = onChainClick
            )
        }
    }
}

@Composable
private fun ChainItem(
    index: Int,
    chain: ChainSelectionUi,
    onChainClick: (Int, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable {
                onChainClick(index, chain.isSelected)
            }
            .conditionalModifier(chain.isSelected) {
                Modifier.coloredShadow(
                    chain.color.toColor(),
                    borderRadius = 8.dp,
                    blurRadius = 8.dp,
                    spread = 2f
                )
            }
            .border(width = 1.dp, color = chain.color.toColor(), shape = RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colors.background, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = chain.icon),
            contentDescription = "${chain.chainName} icon"
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = chain.chainName)
    }
}

@CompletePreviews
@Composable
private fun ChainSelectionScreenPreview(@PreviewParameter(ChainSelectionStateProvider::class) chains: List<ChainSelectionUi>) {
    PreviewTheme {
        ChainSelectionScreen(
            composableScope = rememberCoroutineScope(),
            dispatcher = Dispatchers.Main,
            chains = chains,
            awaitingState = false,
            pairingUri = PairingUri(uri = "", isReCaps = false),
            context = LocalContext.current,
            onDialogDismiss = {},
            onChainClick = { _, _ -> },
            onConnectClick = {},
            onAuthenticateClick = {},
            onAuthenticateSIWEClick = {},
            onAuthenticateLinkMode = {}
        )
    }
}

private class ChainSelectionStateProvider : PreviewParameterProvider<List<ChainSelectionUi>> {
    override val values: Sequence<List<ChainSelectionUi>>
        get() = sequenceOf(
            Chains.values().map { it.toChainUiState() }
        )
}

private const val SAMPLE_WALLET_DEBUG_PACKAGE = "com.walletconnect.sample.wallet.debug"
private const val SAMPLE_WALLET_INTERNAL_PACKAGE = "com.walletconnect.sample.wallet.internal"
private const val SAMPLE_WALLET_RELEASE_PACKAGE = "com.walletconnect.sample.wallet"
private fun Context.isSampleWalletInstalled() =
    (BuildConfig.BUILD_TYPE == "debug" && packageManager.isPackageInstalled(SAMPLE_WALLET_DEBUG_PACKAGE)) ||
            (BuildConfig.BUILD_TYPE == "release" && packageManager.isPackageInstalled(SAMPLE_WALLET_RELEASE_PACKAGE)) ||
            (BuildConfig.BUILD_TYPE == "internal" && packageManager.isPackageInstalled(SAMPLE_WALLET_INTERNAL_PACKAGE))

data class PairingUri(
    val uri: String,
    val isReCaps: Boolean
)