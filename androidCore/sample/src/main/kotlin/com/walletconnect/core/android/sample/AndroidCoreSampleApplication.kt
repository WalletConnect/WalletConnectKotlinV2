package com.walletconnect.core.android.sample

import android.app.Application
import com.walletconnect.android.RelayClient
import com.walletconnect.android.connection.ConnectionType

class AndroidCoreSampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

//        com.walletconnect.android.impl.common.model.Redirect()
        RelayClient.initialize("https://www.google.com", ConnectionType.AUTOMATIC, this)
//        com.walletconnect.foundation.common.model.Topic("")
    }
}