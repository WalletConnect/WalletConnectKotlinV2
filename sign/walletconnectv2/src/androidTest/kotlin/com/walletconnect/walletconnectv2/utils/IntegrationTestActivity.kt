package com.walletconnect.walletconnectv2.utils

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout

class IntegrationTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(LinearLayout(this))
    }
}