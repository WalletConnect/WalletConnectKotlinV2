package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun BoxScope.LazyColumnSurroundedWithFogVertically(modifier: Modifier = Modifier, indexByWhichShouldDisplayBottomFog: Int, lazyColumContent: LazyListScope.() -> Unit) {
    val lazyListState = rememberLazyListState()
    val isScrolled by remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset > 0 } }
    val isScrolledToTheEnd by remember { derivedStateOf { lazyListState.firstVisibleItemIndex >= indexByWhichShouldDisplayBottomFog } }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
    ) {
        lazyColumContent()
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.TopCenter),
        visible = isScrolled,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Spacer(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(MaterialTheme.colors.background, Color.Transparent))
                )
        )
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = !isScrolledToTheEnd,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Spacer(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colors.background))
                )
        )
    }
}