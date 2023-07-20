@file:OptIn(ExperimentalMaterialApi::class)

package com.walletconnect.sample.dapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.dapp.ui.routes.host.DappSampleHost
import com.walletconnect.sample.common.ui.theme.WCSampleAppTheme

class DappSampleActivity : ComponentActivity() {
    @ExperimentalMaterialNavigationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent() {
            WCSampleAppTheme {
                DappSampleHost()
            }
        }
    }
}
