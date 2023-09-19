package com.walletconnect.sample.wallet.ui.routes.dialog_routes.auth_request

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.walletconnect.sample.wallet.ui.common.Buttons
import com.walletconnect.sample.wallet.ui.common.Content
import com.walletconnect.sample.wallet.ui.common.InnerContent
import com.walletconnect.sample.wallet.ui.common.SemiTransparentDialog
import com.walletconnect.sample.wallet.ui.common.peer.Peer
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import com.walletconnect.sample.common.ui.themedColor
import com.walletconnect.sample.wallet.ui.common.peer.getValidationColor
import kotlinx.coroutines.launch

@Composable
fun AuthRequestRoute(navController: NavHostController, authRequestViewModel: AuthRequestViewModel = viewModel()) {
    val authRequestUI = authRequestViewModel.authRequest ?: throw Exception("Missing auth request")
    val composableScope = rememberCoroutineScope()
    val allowButtonColor = getValidationColor(authRequestUI.peerContextUI.validation)
    SemiTransparentDialog {
        Spacer(modifier = Modifier.height(24.dp))
        Peer(peerUI = authRequestUI.peerUI, "would like to connect", authRequestUI.peerContextUI)
        Spacer(modifier = Modifier.height(16.dp))
        Message(authRequestUI = authRequestUI)
        Spacer(modifier = Modifier.height(16.dp))
        Buttons(allowButtonColor, onDecline = {
            composableScope.launch {
                try {
                    authRequestViewModel.reject()
                    navController.popBackStack()
                    navController.showSnackbar("Auth Request declined")
                } catch (e: Throwable) {
                    closeAndShowError(navController, e.message)
                }
            }
        }, onAllow = {
            composableScope.launch {
                try {
                    authRequestViewModel.approve()
                    navController.popBackStack()
                    navController.showSnackbar("Auth Request approved")
                } catch (e: Exception) {
                    closeAndShowError(navController, e.message)
                }
            }
        })
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun closeAndShowError(navController: NavHostController, message: String?) {
    navController.popBackStack()
    navController.showSnackbar(message ?: "Auth request error, please check your Internet connection")
}

@Composable
fun Message(authRequestUI: AuthRequestUI) {
    Content(title = "Message") {
        InnerContent {
            Text(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 13.dp),
                text = authRequestUI.message, style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = themedColor(darkColor = Color(0xFF9ea9a9), lightColor = Color(0xFF788686)))
            )
        }
    }
}

