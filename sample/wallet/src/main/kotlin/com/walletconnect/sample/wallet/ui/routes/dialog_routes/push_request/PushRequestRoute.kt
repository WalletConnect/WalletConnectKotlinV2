package com.walletconnect.sample.wallet.ui.routes.dialog_routes.push_request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.walletconnect.sample.wallet.ui.common.Buttons
import com.walletconnect.sample.wallet.ui.common.SemiTransparentDialog
import com.walletconnect.sample.wallet.ui.common.peer.Peer
import com.walletconnect.sample.wallet.ui.routes.showSnackbar
import com.walletconnect.sample.wallet.ui.common.Content
import com.walletconnect.sample.common.ui.themedColor

@Composable
fun PushRequestRoute(
    navController: NavHostController,
    requestId: Long,
    encodedPeerName: String,
    encodedPeerDesc: String,
    encodedIconUrl: String?,
    encodedRedirect: String?,
    pushRequestViewModel: PushRequestViewModel = viewModel(),
) {
    val pushRequestUI = pushRequestViewModel.generatePushRequestUI(requestId, encodedPeerName, encodedPeerDesc, encodedIconUrl, encodedRedirect)

    SemiTransparentDialog {
        Spacer(modifier = Modifier.height(24.dp))
        Peer(peerUI = pushRequestUI.peerUI, "sends a request")
        Spacer(modifier = Modifier.height(16.dp))
        Request()
        Spacer(modifier = Modifier.height(16.dp))
        Buttons(onDecline = {
            pushRequestViewModel.reject(requestId) {}
            navController.popBackStack()
            navController.showSnackbar("Push Request declined")
        }, onAllow = {
            pushRequestViewModel.approve(requestId) {}
            navController.popBackStack()
            navController.showSnackbar("Push Request approved")
        })
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun Request() {
    Column(modifier = Modifier.wrapContentHeight()) {
        Content(title = "Request") {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, top = 0.dp, end = 5.dp, bottom = 10.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(themedColor(darkColor = Color(0xFFE4E4E7).copy(alpha = .12f), lightColor = Color(0xFF505059).copy(.1f)))
                    .padding(start = 8.dp, top = 5.dp, end = 8.dp, bottom = 5.dp),
                text = "Push Request",
                style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = themedColor(darkColor = Color(0xFF9ea9a9), lightColor = Color(0xFF788686)))
            )
        }
    }
}

