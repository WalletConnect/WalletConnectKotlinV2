package com.walletconnect.web3.modal.ui.components.internal.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.web3.modal.R
import com.walletconnect.web3.modal.ui.previews.MultipleComponentsPreview
import com.walletconnect.web3.modal.ui.previews.UiModePreview
import com.walletconnect.web3.modal.ui.previews.testWallets
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.utils.grayColorFilter

@Composable
internal fun MultipleWalletIcon(wallets: List<Wallet>) {
    Column(
        modifier = Modifier
            .size(40.dp)
            .background(Web3ModalTheme.colors.background.color200, shape = RoundedCornerShape(10.dp))
            .padding(1.dp)
            .border(1.dp, Web3ModalTheme.colors.overlay10, shape = RoundedCornerShape(10.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        wallets.chunked(2).forEach {
            Row(
                modifier = Modifier.width(40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                it.forEach { item ->
                    WalletImage(
                        url = item.imageUrl,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(1.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

    }
}

@Composable
internal fun WalletsLazyGridView(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    content: LazyGridScope.(Int) -> Unit
) {
    val walletItemWidth = 92.dp
    BoxWithConstraints(
        modifier = modifier
    ) {
        val maxColumnsNumber = maxOf((maxWidth / walletItemWidth).toInt(), 1)
        LazyVerticalGrid(
            state = state,
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
    itemsIndexed(
        items = wallets,
        key = { _, wallet -> wallet.id }
    ) { _, wallet ->
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
            .placeholder(R.drawable.wallet_placeholder)
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
    Surface(
        modifier = Modifier.padding(8.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .width(76.dp)
                .height(100.dp)
                .background(Web3ModalTheme.colors.overlay05)
                .clickable { onWalletItemClick(wallet) }
                .padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VerticalSpacer(height = 8.dp)
            WalletImage(
                url = wallet.imageUrl,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            VerticalSpacer(height = 8.dp)
            Text(
                text = wallet.name,
                style = Web3ModalTheme.typo.tiny500,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@UiModePreview
@Composable
private fun PreviewWallets() {
    MultipleComponentsPreview(
        { WalletGridItem(wallet = testWallets.first(), onWalletItemClick = {}) },
        { WalletGridItem(wallet = testWallets[1], onWalletItemClick = {}) },
        { MultipleWalletIcon(wallets = testWallets.take(4)) },
    )
}
