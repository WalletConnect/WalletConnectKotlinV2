@file:OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)

package com.walletconnect.modals.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.walletconnect.modals.common.Route
import com.walletconnect.modals.ui.theme.WalletConnectTheme
import com.walletconnect.web3.modal.ui.web3ModalGraph

class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletConnectTheme {
                val scaffoldState: ScaffoldState = rememberScaffoldState()
                val sheetState = rememberModalBottomSheetState(
                    initialValue = ModalBottomSheetValue.Hidden,
                    skipHalfExpanded = true
                )
                val bottomSheetNavigator = BottomSheetNavigator(sheetState)
                val navController = rememberNavController(bottomSheetNavigator)

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "Jetpack Compose") },
                            navigationIcon = { Icon(imageVector = Icons.Default.ArrowBack, null, modifier = Modifier.clickable { this.finish() }) }
                        )
                    },
                ) { paddingValues ->
                    ModalBottomSheetLayout(
                        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        bottomSheetNavigator = bottomSheetNavigator,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Route.Home.path
                        ) {
                            composable(Route.Home.path) {
                                HomeScreen(navController = navController)
                            }
                            web3ModalGraph(navController)
                        }
                    }
                }
            }
        }
    }
}