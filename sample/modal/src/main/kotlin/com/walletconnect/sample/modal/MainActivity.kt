@file:OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)

package com.walletconnect.sample.modal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.walletconnect.sample.modal.common.Route
import com.walletconnect.sample.modal.compose.ComposeActivity
import com.walletconnect.sample.modal.kotlindsl.KotlinDSLActivity
import com.walletconnect.sample.modal.navComponent.NavComponentActivity
import com.walletconnect.sample.modal.ui.LabScreen
import com.walletconnect.sample.modal.ui.theme.WalletConnectTheme
import com.walletconnect.web3.modal.ui.web3ModalGraph

class MainActivity : ComponentActivity() {
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
                        TopAppBar(title = {
                            Text(
                                text = "Web3Modal Lab",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        })
                    },
                    drawerContent = { DrawerContent() }
                ) { paddingValues ->
                    ModalBottomSheetLayout(
                        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        bottomSheetNavigator = bottomSheetNavigator,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Route.Lab.path
                        ) {
                            composable(Route.Lab.path) {
                                LabScreen(navController = navController)
                            }
                            web3ModalGraph(navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerContent() {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Button(onClick = {
            val intent = Intent(context, ComposeActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Jetpack Compose")
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = {
            val intent = Intent(context, KotlinDSLActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Kotlin DSL")
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = {
            val intent = Intent(context, NavComponentActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(text = "Navigation component")
        }
    }
}
