package com.walletconnect.responder

import android.app.Application

class ResponderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // todo: Do some WC AuthSDK Init magic here
    }
}