@file:OptIn(ExperimentalMaterialApi::class)

package com.walletconnect.sample.dapp.web3modal.ui.previews

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.walletconnect.sample.dapp.web3modal.ui.common.Web3ModalRoot
import com.walletconnect.sample.dapp.web3modal.ui.theme.provideDefaultColors

@Composable
fun Web3ModalPreview(
    content: @Composable () -> Unit
) {
    Web3ModalRoot(
        sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true),
        coroutinesScope = rememberCoroutineScope(),
        navController = rememberNavController(),
        colors = provideDefaultColors()
    ) {
        content()
    }
}