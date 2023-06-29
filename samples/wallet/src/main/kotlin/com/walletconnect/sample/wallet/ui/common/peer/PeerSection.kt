package com.walletconnect.sample.wallet.ui.common.peer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.walletconnect.sample.wallet.R
import com.walletconnect.sample_common.ui.themedColor


@Composable
fun Peer(peerUI: PeerUI, actionText: String?, peerContextUI: PeerContextUI? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            val iconModifier = Modifier
                .size(60.dp)
                .background(color = themedColor(darkColor = Color(0xFFE4E4E7).copy(0.12f), lightColor = Color(0xFF3C3C43).copy(0.12f)), shape = CircleShape)

            if (peerUI.peerIcon.isNotBlank() && peerUI.peerIcon != "null") {
                GlideImage(
                    imageModel = { peerUI.peerIcon },
                    imageOptions = ImageOptions(contentScale = ContentScale.Fit, alignment = Alignment.Center),
                    modifier = iconModifier
                )
            } else {
                Image(modifier = iconModifier.alpha(.7f), imageVector = ImageVector.vectorResource(id = R.drawable.sad_face), contentDescription = "Sad face")
            }

            Image(painter = painterResource(id = R.drawable.emojications), contentDescription = null)
            Image(modifier = Modifier.size(60.dp), painter = painterResource(id = R.drawable.wc_icon), contentDescription = null)
        }
        if (actionText != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = peerUI.peerName.takeIf { it.isNotBlank() } ?: "Dapp", maxLines = 1, style = TextStyle(
                    fontWeight = FontWeight.Bold, fontSize = 22.sp, color = themedColor(Color(0xFFFFFFFF), Color(0xFF000000))
                )
            )
            Text(
                text = actionText, maxLines = 1, style = TextStyle(
                    fontWeight = FontWeight.Medium, fontSize = 22.sp, color = themedColor(Color(0xFFb9b3b5), Color(0xFF484648))
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = peerUI.peerUri, maxLines = 1, style = TextStyle(
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = themedColor(darkColor = Color(0xFFC9C9CF).copy(alpha = .6f), lightColor = Color(0xFF3C3C43).copy(alpha = .6f))
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (peerContextUI != null) {
                Image(modifier = Modifier.size(30.dp), painter = painterResource(id = getValidationIcon(peerContextUI.validation)), contentDescription = null)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun getValidationIcon(validation: Validation): Int {
    return when (validation) {
        Validation.VALID -> R.drawable.green_check
        Validation.INVALID -> R.drawable.red_dangerous
        Validation.UNKNOWN -> R.drawable.orange_warning
    }
}