@file:OptIn(ExperimentalMaterialApi::class)

package com.walletconnect.sample.dapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.sample.common.ui.theme.WCSampleAppTheme
import com.walletconnect.sample.dapp.ui.routes.host.DappSampleHost
import com.walletconnect.sign.client.SignClient

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)


        if (intent?.dataString?.contains("wc_ev") == true) {
            SignClient.dispatchEnvelope(intent.dataString ?: "") {
                println("Dapp Dispatch error: $it")
            }
        }
    }
}
