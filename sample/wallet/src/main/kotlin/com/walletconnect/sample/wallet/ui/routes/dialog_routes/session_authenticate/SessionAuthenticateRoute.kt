package com.walletconnect.sample.wallet.ui.routes.dialog_routes.session_authenticate

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.walletconnect.android.internal.common.exception.NoConnectivityException
import com.walletconnect.sample.common.sendResponseDeepLink
import com.walletconnect.sample.common.ui.theme.mismatch_color
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample.wallet.ui.common.Buttons
import com.walletconnect.sample.wallet.ui.common.SemiTransparentDialog
import com.walletconnect.sample.wallet.ui.common.generated.CancelButton
import com.walletconnect.sample.wallet.ui.common.peer.Peer
import com.walletconnect.sample.wallet.ui.common.peer.getValidationColor
import com.walletconnect.sample.wallet.ui.routes.Route
import com.walletconnect.sample.wallet.ui.routes.composable_routes.connections.ConnectionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SessionAuthenticateRoute(navController: NavHostController, connectionsViewModel: ConnectionsViewModel, sessionAuthenticateViewModel: SessionAuthenticateViewModel = viewModel()) {
    val authenticateRequestUI = sessionAuthenticateViewModel.sessionAuthenticateUI ?: throw Exception("Missing authenticate request")
    val composableScope = rememberCoroutineScope()
    val allowButtonColor = getValidationColor(authenticateRequestUI.peerContextUI.validation)
    val context = LocalContext.current

    var shouldOpenProposalDialog by remember { mutableStateOf(false) }
    if (shouldOpenProposalDialog) {
        SessionAuthenticateDialog(authenticateRequestUI, allowButtonColor, composableScope, sessionAuthenticateViewModel, connectionsViewModel, navController, context)
    }

    if (authenticateRequestUI.peerContextUI.isScam == true && !shouldOpenProposalDialog) {
        ScammerScreen(authenticateRequestUI, navController) { shouldOpenProposalDialog = true }
    } else {
        SessionAuthenticateDialog(authenticateRequestUI, allowButtonColor, composableScope, sessionAuthenticateViewModel, connectionsViewModel, navController, context)
    }

    SessionAuthenticateDialog(authenticateRequestUI, allowButtonColor, composableScope, sessionAuthenticateViewModel, connectionsViewModel, navController, context)
}

@Composable
fun ScammerScreen(
    sessionAuthenticateUI: SessionAuthenticateUI,
    navController: NavHostController,
    openDialog: () -> Unit,
) {
    SemiTransparentDialog(Color(0xFF000000)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .background(mismatch_color.copy(alpha = .15f))
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(modifier = Modifier.size(72.dp), painter = painterResource(R.drawable.ic_scam), contentDescription = null)
            Text(text = "Website flagged", style = TextStyle(color = Color(0xFFFFFFFF), fontSize = 24.sp, fontWeight = FontWeight.Bold))
            Text(text = Uri.parse(sessionAuthenticateUI.peerContextUI.origin).host ?: "", style = TextStyle(color = Color(0xFFC9C9C9)))
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
                modifier = Modifier
                    .padding(16.dp)
                    .height(46.dp)
                    .fillMaxWidth()
                    .clickable {
                        navController.popBackStack(
                            route = Route.Connections.path,
                            inclusive = false
                        )
                    },
                backgroundColor = Color(0xFFFFFFFF).copy(.25f)
            )
        }
    }
}


@Composable
private fun SessionAuthenticateDialog(
    authenticateRequestUI: SessionAuthenticateUI,
    allowButtonColor: Color,
    composableScope: CoroutineScope,
    sessionAuthenticateViewModel: SessionAuthenticateViewModel,
    connectionsViewModel: ConnectionsViewModel,
    navController: NavHostController,
    context: Context,
) {
    var isConfirmLoading by remember { mutableStateOf(false) }
    var isCancelLoading by remember { mutableStateOf(false) }
    SemiTransparentDialog {
        Spacer(modifier = Modifier.height(24.dp))
        Peer(peerUI = authenticateRequestUI.peerUI, "would like to connect", authenticateRequestUI.peerContextUI)
        Spacer(modifier = Modifier.height(16.dp))
        Messages(authRequestUI = authenticateRequestUI)
        Spacer(modifier = Modifier.height(16.dp))
        Buttons(
            allowButtonColor,
            onCancel = {
                isCancelLoading = true

                if (authenticateRequestUI.peerUI.linkMode) {
                    navController.popBackStack(route = Route.Connections.path, inclusive = false)
                    connectionsViewModel.refreshConnections()
                }

                try {
                    sessionAuthenticateViewModel.reject(
                        onSuccess = { redirect ->
                            isCancelLoading = false
                            composableScope.launch(Dispatchers.Main) {
                                navController.popBackStack(route = Route.Connections.path, inclusive = false)
                            }

                            if (redirect.isNotEmpty()) {
                                context.sendResponseDeepLink(redirect.toUri())
                            } else {
                                composableScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, "Go back to your browser", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onError = { error ->
                            isCancelLoading = false
                            showError(navController, error, composableScope, context)
                        })
                } catch (e: Throwable) {
                    showError(navController, e, composableScope, context)
                }
            }, onConfirm = {
                isConfirmLoading = true

                if (authenticateRequestUI.peerUI.linkMode) {
                    navController.popBackStack(route = Route.Connections.path, inclusive = false)
                    connectionsViewModel.refreshConnections()
                }

                try {
                    sessionAuthenticateViewModel.approve(
                        onSuccess = { redirect ->
                            isConfirmLoading = false
                            composableScope.launch(Dispatchers.Main) {
                                navController.popBackStack(route = Route.Connections.path, inclusive = false)
                                connectionsViewModel.refreshConnections()
                            }

                            if (redirect.isNotEmpty()) {
                                context.sendResponseDeepLink(redirect.toUri())
                            } else {
                                composableScope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, "Go back to your browser", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onError = { error ->
                            isConfirmLoading = false
                            showError(navController, error, composableScope, context)
                        })
                } catch (e: Exception) {
                    showError(navController, e, composableScope, context)
                }
            },
            isLoadingConfirm = isConfirmLoading,
            isLoadingCancel = isCancelLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun showError(navController: NavHostController, throwable: Throwable?, coroutineScope: CoroutineScope, context: Context) {
    coroutineScope.launch(Dispatchers.Main) {
        if (throwable !is NoConnectivityException) {
            navController.popBackStack()
        }

        Toast.makeText(context, throwable?.message ?: "Session authenticate error, please check your Internet connection", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun Messages(authRequestUI: SessionAuthenticateUI) {
    LazyColumn(
        modifier = Modifier
            .padding(20.dp)
            .clip(shape = RoundedCornerShape(25.dp))
            .fillMaxWidth()
            .height(300.dp)
            .background(themedColor(darkColor = Color(0xFFE4E4E7).copy(alpha = .12f), lightColor = Color(0xFF505059).copy(.1f)))
    ) {
        itemsIndexed(authRequestUI.messages) { _, item ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item,
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 13.dp),
                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = themedColor(darkColor = Color(0xFF9ea9a9), lightColor = Color(0xFF788686)))
            )
        }
    }
}