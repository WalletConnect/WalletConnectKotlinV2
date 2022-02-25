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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fcvHost) as NavHostFragment
        NavigationUI.setupActionBarWithNavController(this, navHostFragment.navController)
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        DappDelegate.selectedSessionTopic?.let {
            val disconnectParams = WalletConnect.Params.Disconnect(sessionTopic = it, reason = "shutdown", reasonCode = 400)
            WalletConnectClient.disconnect(disconnectParams)
        }

        // re-init scope
//        WalletConnectClient.shutdown()
    }
}

