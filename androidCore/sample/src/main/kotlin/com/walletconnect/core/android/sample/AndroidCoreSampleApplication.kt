package com.walletconnect.core.android.sample

import android.app.Application
import com.walletconnect.android.CoreClient
import com.walletconnect.android.connection.ConnectionType

class AndroidCoreSampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        CoreClient.initialize("https://www.google.com", ConnectionType.AUTOMATIC, this)
    }
}