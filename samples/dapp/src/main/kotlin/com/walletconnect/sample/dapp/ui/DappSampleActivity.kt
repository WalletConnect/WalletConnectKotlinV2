@file:OptIn(ExperimentalMaterialApi::class)

package com.walletconnect.sample.dapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.dapp.ui.routes.Route
import com.walletconnect.sample_common.ui.theme.WCSampleAppTheme

class DappSampleActivity : ComponentActivity() {
    @ExperimentalMaterialNavigationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent() {
            val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
            val bottomSheetNavigator = BottomSheetNavigator(sheetState)
            val navController = rememberNavController(bottomSheetNavigator)
            WCSampleAppTheme {
                Scaffold { innerPadding ->
                    DappSampleNavGraph(
                        sheetState = sheetState,
                        bottomSheetNavigator = bottomSheetNavigator,
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = Route.ChainSelection.path
                    )
                }
            }
        }
    }
}
