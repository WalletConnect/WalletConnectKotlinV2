package com.walletconnect.dapp.ui.host

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.walletconnect.dapp.R
import com.walletconnect.dapp.domain.DappDelegate
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient

class DappSampleActivity : AppCompatActivity(R.layout.activity_dapp) {
    private val navHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NavigationUI.setupActionBarWithNavController(this, navHostFragment.navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()

        DappDelegate.selectedSessionTopic?.let {
            val disconnectParams = WalletConnect.Params.Disconnect(sessionTopic = it, reason = "shutdown", reasonCode = 400)
            WalletConnectClient.disconnect(disconnectParams)
        }
    }
}

