package com.walletconnect.wcmodal.ui.routes.get_wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.modal.R
import com.walletconnect.modal.utils.openPlayStore
import com.walletconnect.wcmodal.ui.components.ModalTopBar
import com.walletconnect.wcmodal.ui.components.RoundedMainButton
import com.walletconnect.wcmodal.ui.theme.ModalTheme

@Composable
internal fun GetAWalletRoute(
    navController: NavController,
    wallets: List<Wallet>,
) {
    GetAWalletContent(
        wallets = wallets,
        onBackPressed = navController::popBackStack,
    )
}

@Composable
private fun GetAWalletContent(
    onBackPressed: () -> Unit,
    wallets: List<Wallet>,
) {
    val uriHandler = LocalUriHandler.current

    Column {
        ModalTopBar(title = "Get a wallet", onBackPressed = onBackPressed)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            itemsIndexed(wallets.take(6)) { _, wallet ->
                WalletListItem(wallet = wallet)
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Not what you're looking for?", style = TextStyle(
                            color = ModalTheme.colors.textColor,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "With hundreds of wallets out there, there’s something for everyone",
                        style = TextStyle(
                            color = ModalTheme.colors.secondaryTextColor,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    RoundedMainButton(
                        text = "Explore Wallets",
                        onClick = { uriHandler.openUri("https://explorer.walletconnect.com/?type=wallet") },
                        endIcon = {
                            Image(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_external_link),
                                colorFilter = ColorFilter.tint(ModalTheme.colors.onMainColor),
                                contentDescription = null,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletListItem(wallet: Wallet) {
    val uriHandler = LocalUriHandler.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(wallet.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = wallet.name,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = ModalTheme.colors.onBackgroundColor,
                ),
                modifier = Modifier.weight(1f)
            )
            RoundedMainButton(
                text = "Get",
                onClick = { uriHandler.openPlayStore(wallet.playStoreLink) },
                endIcon = {
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_forward_chevron),
                        colorFilter = ColorFilter.tint(ModalTheme.colors.onMainColor),
                        contentDescription = null,
                    )
                }
            )
        }
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(ModalTheme.colors.dividerColor)
        )
    }
}
