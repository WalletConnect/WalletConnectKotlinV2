@file:OptIn(ExperimentalMaterialApi::class)

package com.walletconnect.sample.dapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.walletconnect.sample.dapp.ui.routes.Route
import com.walletconnect.sample_common.theme.WCSampleAppTheme

class DappSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent() {
            val navController = rememberNavController()
            WCSampleAppTheme {
                Scaffold { innerPadding ->
                    DappSampleNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = Route.ChainSelection.path
                    )
                }
            }
        }
    }
}
