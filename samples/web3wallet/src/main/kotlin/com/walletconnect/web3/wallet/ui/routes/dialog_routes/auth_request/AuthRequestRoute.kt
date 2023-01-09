package com.walletconnect.web3.wallet.ui.routes.dialog_routes.auth_request

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.walletconnect.web3.wallet.ui.common.*
import com.walletconnect.web3.wallet.ui.common.peer.Peer
import com.walletconnect.web3.wallet.ui.routes.showSnackbar

@Composable
fun AuthRequestRoute(navController: NavHostController, authRequestViewModel: AuthRequestViewModel = viewModel()) {
    val authRequestUI = authRequestViewModel.authRequest ?: throw Exception("Missing auth request")
    SemiTransparentDialog {
        Spacer(modifier = Modifier.height(24.dp))
        Peer(peerUI = authRequestUI.peerUI, "would like to connect")
        Spacer(modifier = Modifier.height(16.dp))
        Message(authRequestUI = authRequestUI)
        Spacer(modifier = Modifier.height(16.dp))
        Buttons(onDecline = {
            authRequestViewModel.reject()
            navController.popBackStack()
            navController.showSnackbar("Auth Request declined")
        }, onAllow = {
            authRequestViewModel.approve()
            navController.popBackStack()
            navController.showSnackbar("Auth Request approved")
        })
        Spacer(modifier = Modifier.height(16.dp))
    }
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

