package com.walletconnect.requester

import android.app.Application

class RequesterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // todo: Do some WC AuthSDK Init magic here
    }
}