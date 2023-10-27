package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_proposal

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.sample.common.Chains
import com.walletconnect.sample.common.CompletePreviews
import com.walletconnect.sample.common.sendResponseDeepLink
import com.walletconnect.sample.common.ui.theme.PreviewTheme
import com.walletconnect.sample.common.ui.theme.mismatch_color
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.common.Buttons
import com.walletconnect.sample.wallet.ui.common.SemiTransparentDialog
import com.walletconnect.sample.wallet.ui.common.generated.CancelButton
import com.walletconnect.sample.wallet.ui.common.peer.Peer
import com.walletconnect.sample.wallet.ui.common.peer.PeerContextUI
import com.walletconnect.sample.wallet.ui.common.peer.Validation
import com.walletconnect.sample.wallet.ui.common.peer.getColor
import com.walletconnect.sample.wallet.ui.common.peer.getDescriptionContent
import com.walletconnect.sample.wallet.ui.common.peer.getDescriptionTitle
import com.walletconnect.sample.wallet.ui.common.peer.getValidationColor
import com.walletconnect.sample.wallet.ui.common.peer.getValidationIcon
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@CompletePreviews
@Composable
fun SessionProposalRoutePreview() {
    PreviewTheme {
        SessionProposalRoute(rememberNavController())
    }
}

@Composable
fun SessionProposalRoute(navController: NavHostController, sessionProposalViewModel: SessionProposalViewModel = viewModel()) {
    val sessionProposalUI = sessionProposalViewModel.sessionProposal ?: throw Exception("Missing session proposal")
    val composableScope = rememberCoroutineScope()
    val context = LocalContext.current
    val allowButtonColor = getColor(sessionProposalUI.peerContext)
    var shouldOpenProposalDialog by remember { mutableStateOf(false) }
    if (shouldOpenProposalDialog) {
        SessionProposalDialog(sessionProposalUI, allowButtonColor, composableScope, sessionProposalViewModel, context, navController)
    }

    if (sessionProposalUI.peerContext.isScam == true && !shouldOpenProposalDialog) {
        ScammerScreen(sessionProposalUI, navController) { shouldOpenProposalDialog = true }
    } else {
        SessionProposalDialog(sessionProposalUI, allowButtonColor, composableScope, sessionProposalViewModel, context, navController)
    }
}

@Composable
fun ScammerScreen(
    sessionProposalUI: SessionProposalUI,
    navController: NavHostController,
    openDialog: () -> Unit
) {
    SemiTransparentDialog(Color(0xFF000000)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(mismatch_color.copy(alpha = .15f)).fillMaxWidth()) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(modifier = Modifier.size(72.dp), painter = painterResource(R.drawable.ic_scam), contentDescription = null)
            Text(text = "Website flagged", style = TextStyle(color = Color(0xFFFFFFFF), fontSize = 24.sp, fontWeight = FontWeight.Bold))
            Text(text = Uri.parse(sessionProposalUI.peerContext.origin).host ?: "", style = TextStyle(color = Color(0xFFC9C9C9)))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                text = "The website you're trying to connect with is flagged as malicious by multiple security providers. Approving may lead to loss of funds.",
                style = TextStyle(color = Color(0xFFFFFFFF), textAlign = TextAlign.Center)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Proceed anyway", modifier = Modifier.clickable { openDialog() }, style = TextStyle(color = mismatch_color, fontSize = 16.sp))
            Spacer(modifier = Modifier.height(16.dp))
            CancelButton(
                modifier = Modifier.padding(16.dp).height(46.dp).fillMaxWidth().clickable { navController.popBackStack(route = Route.Connections.path, inclusive = false) },
                backgroundColor = Color(0xFFFFFFFF).copy(.25f)
            )
        }
    }
}

@Composable
private fun SessionProposalDialog(
    sessionProposalUI: SessionProposalUI,
    allowButtonColor: Color,
    coroutineScope: CoroutineScope,
    sessionProposalViewModel: SessionProposalViewModel,
    context: Context,
    navController: NavHostController
) {
    SemiTransparentDialog {
        Spacer(modifier = Modifier.height(24.dp))
        Peer(peerUI = sessionProposalUI.peerUI, "wants to connect", sessionProposalUI.peerContext)
        Spacer(modifier = Modifier.height(18.dp))
        Permissions(sessionProposalUI = sessionProposalUI)
        Spacer(modifier = Modifier.height(18.dp))
        AccountAndNetwork(sessionProposalUI)
        Spacer(modifier = Modifier.height(18.dp))
        Buttons(allowButtonColor, onDecline = {
            coroutineScope.launch {
                try {
                    sessionProposalViewModel.reject(sessionProposalUI.pubKey) { redirect ->
                        if (redirect.isNotEmpty()) {
                            context.sendResponseDeepLink(redirect.toUri())
                        }
                    }
                    navController.popBackStack(route = Route.Connections.path, inclusive = false)
                } catch (e: Throwable) {
                    closeAndShowError(navController, e.message)
                }
            }
        }, onAllow = {
            coroutineScope.launch {
                try {
                    sessionProposalViewModel.approve(sessionProposalUI.pubKey) { redirect ->
                        if (redirect.isNotEmpty()) {
                            context.sendResponseDeepLink(redirect.toUri())
                        }

                        coroutineScope.launch(Dispatchers.Main) {
                            navController.popBackStack(route = Route.Connections.path, inclusive = false)
                        }
                    }
                } catch (e: Throwable) {
                    closeAndShowError(navController, e.message)
                }
            }
        })
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun closeAndShowError(navController: NavHostController, mesage: String?) {
    navController.popBackStack(route = Route.Connections.path, inclusive = false)
    navController.showSnackbar(mesage ?: "Session proposal error, please check your Internet connection")
}

@Composable
fun AccountAndNetwork(sessionProposalUI: SessionProposalUI) {
    val requiredChains = getRequiredChains(sessionProposalUI)
    val optionalChains = getOptionalChains(sessionProposalUI)
    val chains = if (requiredChains.isEmpty()) optionalChains else requiredChains

    val network = Chains.values().find { chain -> chain.chainId == chains.first() }
    val account = walletMetaData.namespaces.values.first().accounts.find { account -> account.contains(chains.first()) }?.split(":")?.last()

    Row(modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(horizontalAlignment = Alignment.Start) {
            Text("Account", style = TextStyle(color = Color(0xFFC9C9C9), fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
            Row(verticalAlignment = Alignment.Bottom) {
                Image(
                    modifier = Modifier.size(24.dp).padding(end = 4.dp),
                    painter = painterResource(id = R.drawable.wc_icon_round),
                    contentDescription = null
                )
                Text(account?.toVisibleAddress() ?: "", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("Chain", style = TextStyle(color = Color(0xFFC9C9C9), fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
            Row(verticalAlignment = Alignment.Bottom) {
                Image(
                    modifier = Modifier.size(24.dp).padding(end = 4.dp),
                    painter = painterResource(id = network?.icon ?: R.drawable.wc_icon_round),
                    contentDescription = null
                )
                Text(network?.chainName ?: "", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

private fun getOptionalChains(sessionProposalUI: SessionProposalUI) = sessionProposalUI.optionalNamespaces.flatMap { (namespaceKey, proposal) -> proposal.chains ?: listOf(namespaceKey) }

private fun getRequiredChains(sessionProposalUI: SessionProposalUI) = sessionProposalUI.namespaces.flatMap { (namespaceKey, proposal) -> proposal.chains ?: listOf(namespaceKey) }

@Composable
fun Permissions(sessionProposalUI: SessionProposalUI) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RequestedPermissions()
        if (sessionProposalUI.peerContext.validation != Validation.VALID) {
            Spacer(modifier = Modifier.height(16.dp))
            ValidationDescription(sessionProposalUI.peerContext)
        }
    }
}

@Composable
private fun ValidationDescription(peerContextUI: PeerContextUI) {
    Row(
        modifier = Modifier
            .padding(end = 20.dp, start = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(color = getColor(peerContextUI).copy(alpha = 0.15f))
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = peerContextUI.isScam?.let { if (it) R.drawable.ic_scam else getValidationIcon(peerContextUI.validation) } ?: getValidationIcon(
                    peerContextUI.validation
                )),
                contentDescription = null)
        }

        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.Start) {
            Text(getDescriptionTitle(peerContextUI), style = TextStyle(color = getColor(peerContextUI), fontWeight = FontWeight.Bold, fontSize = 14.sp))
            Text(getDescriptionContent(peerContextUI), style = TextStyle(fontSize = 14.sp))
        }
    }
}

@Composable
private fun RequestedPermissions() {
    Column(
        modifier = Modifier
            .padding(end = 20.dp, start = 20.dp)
            .border(border = BorderStroke(1.dp, Color(0xFFC9C9C9)), shape = RoundedCornerShape(24.dp))
            .fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp),
            text = "Requested permissions",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = themedColor(Color(0xFF000000), Color(0xFF3C3C43)))
        )
        Spacer(modifier = Modifier.height(4.dp))
        PermissionRow("View your balance and activity")
        PermissionRow("Send approval requests")
        PermissionRow("Move funds without permissions", icon = R.drawable.ic_close, color = Color(0xFFC9C9C9))
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionRow(title: String, icon: Int = R.drawable.ic_check, color: Color = Color(0xFF000000)) {
    Row(modifier = Modifier.padding(start = 12.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(modifier = Modifier.size(18.dp).padding(end = 4.dp), imageVector = ImageVector.vectorResource(id = icon), contentDescription = "check")
        Text(title, style = TextStyle(fontSize = 14.sp, color = color))
    }
}

internal fun String.toVisibleAddress() = "${take(4)}...${takeLast(4)}"