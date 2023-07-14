package com.walletconnect.modal.ui.routes.help

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.walletconnect.modal.R
import com.walletconnect.modal.ui.components.ModalTopBar
import com.walletconnect.modal.ui.components.RoundedMainButton
import com.walletconnect.modal.ui.components.common.HorizontalSpacer
import com.walletconnect.modal.ui.components.common.VerticalSpacer
import com.walletconnect.modal.ui.navigation.Route
import com.walletconnect.modal.ui.preview.ModalPreview
import com.walletconnect.modal.ui.theme.ModalTheme

@Composable
internal fun HelpRoute(
    navController: NavController
) {
    HelpContent(
        onBackPressed = navController::popBackStack,
        onGetWalletClick = { navController.navigate(Route.GetAWallet.path) }
    )
}

@Composable
private fun HelpContent(
    onBackPressed: () -> Unit,
    onGetWalletClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        ModalTopBar(
            title = "What is wallet?",
            onBackPressed = onBackPressed
        )
        Column(modifier = Modifier.padding(horizontal = 32.dp)) {
            HelpSection(
                title = "A home for your digital assets",
                body = "A wallet lets you store, send and receive digital assets like cryptocurrencies and NFTs.",
                assets = listOf(R.drawable.defi, R.drawable.nft, R.drawable.eth)
            )
            VerticalSpacer(4.dp)
            HelpSection(
                title = "One login for all of web3",
                body = "Log in to any app by connecting your wallet. Say goodbye to countless passwords!",
                assets = listOf(R.drawable.login, R.drawable.profile, R.drawable.lock)
            )
            VerticalSpacer(4.dp)
            HelpSection(
                title = "Your gateway to a new web",
                body = "With your wallet, you can explore and interact with DeFi, NFTs, DAOs, and much more.",
                assets = listOf(R.drawable.browser, R.drawable.noun, R.drawable.dao)
            )
            VerticalSpacer(4.dp)
            HelpButtonRow(onGetWalletClick)
            VerticalSpacer(20.dp)
        }
    }
}

@Composable
private fun HelpButtonRow(
    onGetWalletClick: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        RoundedMainButton(
            text = "Get a Wallet",
            onClick = onGetWalletClick,
            startIcon = {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_wallet),
                    contentDescription = "Wallet icon",
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(color = ModalTheme.colors.onMainColor)
                )
            }
        )
        HorizontalSpacer(10.dp)
        RoundedMainButton(
            text = "Learn More",
            onClick = {
                uriHandler.openUri("https://ethereum.org/en/wallets/")
            },
            endIcon = {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_external_link),
                    contentDescription = "external link icon",
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(color = ModalTheme.colors.onMainColor)
                )
            }
        )
    }
}

@Composable
private fun HelpSection(
    title: String,
    body: String,
    assets: List<Int>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            assets.forEach { vectorRes ->
                Image(
                    imageVector = ImageVector.vectorResource(id = vectorRes),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .size(60.dp)
                )
            }
        }
        VerticalSpacer(4.dp)
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                color = ModalTheme.colors.textColor,
                textAlign = TextAlign.Center
            )
        )
        VerticalSpacer(4.dp)
        Text(
            text = body,
            style = TextStyle(
                fontSize = 14.sp,
                color = ModalTheme.colors.secondaryTextColor,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
@Preview
private fun HelpContentPreview() {
    ModalPreview {
        HelpContent({}, {})
    }
}