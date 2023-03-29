package com.walletconnect.web3.inbox.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.inbox.sample.R

class Web3InboxXMLActivity : AppCompatActivity(R.layout.activity_web3inbox) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://developer.android.com/jetpack/compose/migrate/interoperability-apis/compose-in-views
        findViewById<ComposeView>(R.id.composeView).apply {
            setContent {
                val state = Web3Inbox.rememberWeb3InboxState()

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Web3Inbox.View(state = state)
                }
            }
        }
    }
}
