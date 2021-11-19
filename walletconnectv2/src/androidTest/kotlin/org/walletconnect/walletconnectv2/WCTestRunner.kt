package org.walletconnect.walletconnectv2

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class WCTestRunner: AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, IntegrationTestApplication::class.java.name, context)
    }
}