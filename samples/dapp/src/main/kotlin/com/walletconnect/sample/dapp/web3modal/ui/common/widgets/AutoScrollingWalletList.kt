package com.walletconnect.sample.dapp.web3modal.ui.common.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.sample.dapp.web3modal.domain.model.WalletRecommendation
import com.walletconnect.sample.dapp.web3modal.ui.previews.walletsRecommendations
import com.walletconnect.sample_common.ui.theme.PreviewTheme
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach

@Composable
fun AutoScrollingWalletList(
    data: List<WalletRecommendation> = listOf()
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
                    .data(item.images.medium)
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
