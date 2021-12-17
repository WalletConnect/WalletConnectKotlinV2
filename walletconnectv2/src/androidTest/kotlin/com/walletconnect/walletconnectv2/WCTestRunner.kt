package com.walletconnect.walletconnectv2

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.walletconnect.walletconnectv2.utils.IntegrationTestApplication

class WCTestRunner: AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, IntegrationTestApplication::class.java.name, context)
    }
}