package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.web3.modal.domain.model.Wallet
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
internal fun AutoScrollingWalletList(
    data: List<Wallet> = listOf()
) {
    val listState = rememberLazyListState()
    val itemSize = 80.dp
    val density = LocalDensity.current
    val itemSizePx = with(density) { itemSize.toPx() }

    LaunchedEffect(Unit) {
        (0..Int.MAX_VALUE)
            .asSequence()
            .asFlow()
            .onEach {
                listState.animateScrollBy(
                    value = itemSizePx,
                    animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
                )
            }
            .collect()
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        state = listState,
        userScrollEnabled = false
    ) {
        items(Int.MAX_VALUE) {
            val index = it % data.size
            val item = data[index]
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(itemSize)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .alpha(0.5f)
            )
        }
    }
}
