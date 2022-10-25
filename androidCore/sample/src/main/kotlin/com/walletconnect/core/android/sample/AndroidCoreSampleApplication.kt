package com.walletconnect.core.android.sample

import android.app.Application
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.android.relay.RelayClient

class AndroidCoreSampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        RelayClient.initialize("https://www.google.com", ConnectionType.AUTOMATIC, this)
    }
}