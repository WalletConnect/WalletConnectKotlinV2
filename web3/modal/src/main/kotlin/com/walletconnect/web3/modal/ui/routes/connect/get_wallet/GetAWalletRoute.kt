package com.walletconnect.web3.modal.ui.routes.connect.get_wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.walletconnect.web3.modal.ui.components.internal.commons.RoundedMainButton
import com.walletconnect.web3.modal.ui.components.internal.Web3ModalTopBar
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme
import com.walletconnect.web3.modal.R

@Composable
internal fun GetAWalletRoute(navController: NavController) {
    GetAWalletContent(
        onBackPressed = navController::popBackStack,

        )
}

@Composable
private fun GetAWalletContent(
    onBackPressed: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Column {
        Web3ModalTopBar(title = "Get a wallet", onBackPressed = onBackPressed)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Not what you're looking for?", style = TextStyle(
                    color = Web3ModalTheme.colors.textColor,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "With hundreds of wallets out there, there’s something for everyone",
                style = TextStyle(
                    color = Web3ModalTheme.colors.secondaryTextColor,
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
                        colorFilter = ColorFilter.tint(Web3ModalTheme.colors.onMainColor),
                        contentDescription = null,
                    )
                }
            )
        }
    }
}

@Composable
private fun WalletListItem() {
    val url = "https://play.google.com/store/apps/details?id=me.rainbow"
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
                    .data("https://explorer-api.walletconnect.com/v3/logo/md/7a33d7f1-3d12-4b5c-f3ee-5cd83cb1b500?projectId=a7f155fbc59c18b6ad4fb5650067dd41")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Wallet Name",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Web3ModalTheme.colors.onBackgroundColor,
                ),
                modifier = Modifier.weight(1f)
            )
            RoundedMainButton(
                text = "Get",
                onClick = { uriHandler.openUri(url) },
                endIcon = {
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_forward_chevron),
                        colorFilter = ColorFilter.tint(Web3ModalTheme.colors.onMainColor),
                        contentDescription = null,
                    )
                }
            )
        }
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Web3ModalTheme.colors.dividerColor)
        )
    }
}
