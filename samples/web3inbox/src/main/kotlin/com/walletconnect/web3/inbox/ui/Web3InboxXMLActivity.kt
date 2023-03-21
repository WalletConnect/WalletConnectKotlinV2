package com.walletconnect.web3.inbox.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.inbox.sample.R

class Web3InboxXMLActivity : AppCompatActivity(R.layout.activity_web3inbox) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://developer.android.com/jetpack/compose/migrate/interoperability-apis/compose-in-views
        findViewById<ComposeView>(R.id.composeView).apply {
            setContent {
                Web3Inbox.View()
            }
        }
    }
}
