package com.walletconnect.wcmodal.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.ui.components.common.VerticalSpacer
import com.walletconnect.wcmodal.ui.theme.ModalTheme

@Composable
internal fun WalletsLazyGridView(
    modifier: Modifier = Modifier,
    content: LazyGridScope.(Int) -> Unit
) {
    val walletItemWidth = 100.dp
    BoxWithConstraints(
        modifier = modifier
    ) {
        val maxColumnsNumber = maxOf((maxWidth / walletItemWidth).toInt(), 1)
        LazyVerticalGrid(
            columns = GridCells.Fixed(maxColumnsNumber),
            content = {
                content(maxColumnsNumber)
            }
        )
    }
}

internal fun LazyGridScope.walletsGridItems(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit
) {
    itemsIndexed(wallets) { _, wallet ->
        WalletListItem(
            wallet = wallet,
            onWalletItemClick = onWalletItemClick
        )
    }
}

@Composable
internal fun WalletImage(url: String, modifier: Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
internal fun WalletListItem(
    wallet: Wallet,
    onWalletItemClick: (Wallet) -> Unit
) {
    Column(
        modifier = Modifier.clickable { onWalletItemClick(wallet) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WalletImage(
            url = wallet.imageUrl,
            modifier = Modifier
                .size(80.dp)
                .padding(10.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, color = ModalTheme.colors.border, RoundedCornerShape(14.dp))
        )
        Text(
            text = wallet.name,
            style = TextStyle(color = ModalTheme.colors.onBackgroundColor, fontSize = 12.sp),
            textAlign = TextAlign.Center
        )
        VerticalSpacer(height = 16.dp)
    }
}
