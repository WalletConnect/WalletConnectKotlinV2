package com.walletconnect.web3.inbox.sample.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.walletconnect.web3.inbox.client.Web3Inbox
import com.walletconnect.web3.inbox.sample.R

class Web3InboxXMLActivity : AppCompatActivity(R.layout.activity_web3inbox) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: Skipped frames!  The application may be doing too much work on its main thread.
        findViewById<ConstraintLayout>(R.id.root).apply {
            addView(Web3Inbox.getView(applicationContext))
        }
    }
}
