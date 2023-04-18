package com.walletconnect.sample.dapp.web3modal.ui.routes.help

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.walletconnect.sample.dapp.R
import com.walletconnect.sample.dapp.web3modal.ui.common.widgets.RoundedMainButton
import com.walletconnect.sample.dapp.web3modal.ui.common.widgets.Web3ModalTopBar
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalTheme
import com.walletconnect.sample_common.ui.theme.PreviewTheme

@Composable
fun HelpRoute(
    navController: NavController
) {
    HelpContent { navController.popBackStack() }
}

@Composable
private fun HelpContent(
    onBackPressed: () -> Unit
) {
    Column {
        Web3ModalTopBar(
            title = "What is wallet?",
            onBackPressed = onBackPressed
        )
        Column(modifier = Modifier.padding(horizontal = 32.dp)) {
            HelpSection(
                title = "A home for your digital assets",
                body = "A wallet lets you store, send and receive digital assets like cryptocurrencies and NFTs.",
                assets = listOf(R.drawable.defi, R.drawable.nft, R.drawable.eth)
            )
            Spacer(modifier = Modifier.height(4.dp))
            HelpSection(
                title = "One login for all of web3",
                body = "Log in to any app by connecting your wallet. Say goodbye to countless passwords!",
                assets = listOf(R.drawable.login, R.drawable.profile, R.drawable.lock)
            )
            Spacer(modifier = Modifier.height(4.dp))
            HelpSection(
                title = "Your gateway to a new web",
                body = "With your wallet, you can explore and interact with DeFi, NFTs, DAOs, and much more.",
                assets = listOf(R.drawable.browser, R.drawable.noun, R.drawable.dao)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HelpButtonRow()
        }
    }
}

@Composable
fun HelpButtonRow() {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundedMainButton(
            text = "Get a Wallet",
            onClick = {
                // Navigate to get a wallet screen
            },
            modifier = Modifier.weight(1f),
            startIcon = {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_wallet),
                    contentDescription = "Wallet icon",
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(color = Web3ModalTheme.colors.onMainColor)
                )
            }
        )
        Spacer(modifier = Modifier.width(10.dp))
        RoundedMainButton(
            text = "Learn More",
            onClick = {
                uriHandler.openUri("https://ethereum.org/en/wallets/")
            },
            modifier = Modifier.weight(1f),
            endIcon = {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_external_link),
                    contentDescription = "external link icon",
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(color = Web3ModalTheme.colors.onMainColor)
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                color = Web3ModalTheme.colors.textColor,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            style = TextStyle(
                fontSize = 14.sp,
                color = Web3ModalTheme.colors.secondaryTextColor,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
@Preview
private fun HelpContentPreview() {
    PreviewTheme {
        HelpContent {}
    }
}