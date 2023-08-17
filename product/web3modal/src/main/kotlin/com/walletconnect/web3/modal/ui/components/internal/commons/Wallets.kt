package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
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
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.WalletConnectLogo
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

internal fun LazyGridScope.walletsGridItems(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit
) {
    itemsIndexed(wallets) { _, wallet ->
        WalletGridItem(
            wallet = wallet,
            onWalletItemClick = onWalletItemClick
        )
    }
}
@Composable
internal fun WalletImage(url: String, isEnabled: Boolean = true, modifier: Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier,
        colorFilter = if (isEnabled) null else grayColorFilter
    )
}

@Composable
internal fun WalletGridItem(
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
                .clip(RoundedCornerShape(10.dp))
        )
        Text(
            text = wallet.name,
            style = TextStyle(color = Web3ModalTheme.colors.foreground.color100, fontSize = 12.sp),
            textAlign = TextAlign.Center
        )
        VerticalSpacer(height = 16.dp)
    }
}
