package com.walletconnect.wcmodal.ui.routes.all_wallets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.wcmodal.ui.components.ModalSearchTopBar
import com.walletconnect.wcmodal.ui.components.WalletsLazyGridView
import com.walletconnect.wcmodal.ui.components.walletsGridItems
import com.walletconnect.wcmodal.ui.preview.ModalPreview
import com.walletconnect.wcmodal.ui.theme.ModalTheme
import com.walletconnect.modal.utils.isLandscape
import com.walletconnect.wcmodal.ui.navigation.Route

@Composable
internal fun AllWalletsRoute(
    navController: NavController,
    wallets: List<Wallet>
) {
    AllWalletsContent(
        wallets = wallets,
        onWalletItemClick = { navController.navigate(Route.OnHold.path + "/${it.id}") },
        onBackClick = navController::popBackStack
    )
}

@Composable
private fun AllWalletsContent(
    wallets: List<Wallet>,
    onWalletItemClick: (Wallet) -> Unit,
    onBackClick: () -> Unit,
) {
    val gridFraction = if (isLandscape) 1f else .9f
    var searchInputValue by rememberSaveable() { mutableStateOf("") }
    val color = ModalTheme.colors.background

    Column(
        modifier = Modifier
            .fillMaxHeight(gridFraction)
            .padding(horizontal = 4.dp),
    ) {
        ModalSearchTopBar(
            searchValue = searchInputValue,
            onSearchValueChange = {
                searchInputValue = it
            },
            onBackPressed = onBackClick,
        )
        val searchedWallets = wallets.filteredWallets(searchInputValue)
        if (searchedWallets.isEmpty()) {
            NoWalletsFoundItem()
        } else {
            WalletsLazyGridView(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .graphicsLayer { alpha = 0.99f }
                    .drawWithContent {
                        val colors = listOf(
                            Color.Transparent,
                            color
                        )
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(colors, startY = 0f, endY = 40f),
                            blendMode = BlendMode.DstIn,
                        )
                    }
            ) {
                walletsGridItems(searchedWallets, onWalletItemClick)
            }
        }
    }
}

@Composable
fun NoWalletsFoundItem() {
    Text(
        text = "No wallets found",
        style = TextStyle(color = ModalTheme.colors.secondaryTextColor, fontSize = 16.sp),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp)
    )
}

private fun List<Wallet>.filteredWallets(value: String): List<Wallet> = this.filter { it.name.startsWith(prefix = value, ignoreCase = true) }

@Preview
@Composable
private fun AllWalletsPreview() {
    ModalPreview {
        AllWalletsContent(listOf(), {}, {})
    }
}
