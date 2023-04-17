@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)

package com.walletconnect.sample.dapp.web3modal.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.sample.dapp.web3modal.ui.common.widgets.CloseIconButton
import com.walletconnect.sample.dapp.web3modal.ui.common.widgets.QuestionMarkIconButton
import com.walletconnect.sample.dapp.web3modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalColors
import com.walletconnect.sample.dapp.web3modal.ui.theme.Web3ModalTheme
import com.walletconnect.sample.dapp.web3modal.ui.theme.provideDefaultColors
import com.walletconnect.sample.dapp.web3modal.ui.common.widgets.WalletConnectLogo
import com.walletconnect.sample_common.ui.theme.PreviewTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun Web3ModalRoot(
    sheetState: ModalBottomSheetState,
    coroutinesScope: CoroutineScope,
    navController: NavController,
    colors: Web3ModalColors,
    content: @Composable () -> Unit
) {

    LaunchedEffect(key1 = Unit) {
        sheetState.show()
    }

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Bottom
    ) {
        ProvideWeb3ModalThemeComposition(colors = colors) {
            Column(
                modifier = Modifier.background(Web3ModalTheme.colors.mainColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WalletConnectLogo(modifier = Modifier.weight(1f))
                    QuestionMarkIconButton(navController)
                    Spacer(modifier = Modifier.width(16.dp))
                    CloseIconButton {
                        coroutinesScope.launch { sheetState.hide() }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Web3ModalTheme.colors.background,
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    content()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Preview(device = Devices.PIXEL_2_XL)
@Composable
private fun PreviewWeb3ModalRoot() {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    PreviewTheme {
        Web3ModalRoot(sheetState, scope, navController, provideDefaultColors()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Blue)
            )
        }
    }
}