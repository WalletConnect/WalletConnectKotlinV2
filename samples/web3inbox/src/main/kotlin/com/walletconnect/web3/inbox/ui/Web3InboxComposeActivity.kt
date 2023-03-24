package com.walletconnect.web3.inbox.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.inbox.ui.theme.Web3InboxTheme

class Web3InboxComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Web3InboxTheme {
                val state = Web3Inbox.rememberWeb3InboxState()

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Web3Inbox.View(state = state)
                }
            }
        }
    }
}
