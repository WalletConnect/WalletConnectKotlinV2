package com.walletconnect.web3.inbox.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.inbox.sample.R

class Web3InboxXMLActivity : AppCompatActivity(R.layout.activity_web3inbox) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<ConstraintLayout>(R.id.root).apply {
            addView(Web3Inbox.View(applicationContext))
        }
    }
}
