package com.walletconnect.sample.wallet.ui.common.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.sample.wallet.R

@Composable
fun NotificationIcon(size: Dp, url: String?) {
    val hasIcon = remember { mutableStateOf(!url.isNullOrEmpty()) }

    AsyncImage(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (hasIcon.value) Color.Transparent
                else Color(0xFFE2FDFF)
            ),
        model = ImageRequest.Builder(LocalContext.current)
            .data(url.takeIf { hasIcon.value })
            .fallback(R.drawable.ic_globe)
            .error(R.drawable.ic_globe)
            .crossfade(200)
            .listener(
                onError = { _, _ ->
                    hasIcon.value = false
                }
            )
            .build(),
        contentScale = if (hasIcon.value) ContentScale.Fit else ContentScale.None,
        contentDescription = null,
    )
}