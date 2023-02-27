package com.walletconnect.web3.inbox.sample.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.inbox.sample.ui.theme.Web3InboxTheme

class Web3InboxComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Web3InboxTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    //TODO: Skipped frames!  The application may be doing too much work on its main thread.
                    Web3Inbox.View()
                }
            }
        }
    }
}
