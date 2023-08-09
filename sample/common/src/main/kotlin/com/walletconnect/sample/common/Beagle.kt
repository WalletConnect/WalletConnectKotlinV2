package com.walletconnect.sample.common

import android.app.Application
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.log.BeagleLogger
import com.pandulapeter.beagle.logOkHttp.BeagleOkHttpLogger
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.LogListModule
import com.pandulapeter.beagle.modules.NetworkLogListModule
import com.pandulapeter.beagle.modules.ScreenCaptureToolboxModule
import com.pandulapeter.beagle.modules.TextModule
import timber.log.Timber

fun initBeagle(app: Application, header: HeaderModule) {
    Timber.plant(
        object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                Beagle.log("${tag?.let { "[$it] " } ?: ""}$message", "Timber", t?.stackTraceToString(), timestamp = System.currentTimeMillis())
            }
        }
    )

    Beagle.initialize(
        application = app,
        behavior = Behavior(
            logBehavior = Behavior.LogBehavior(loggers = listOf(BeagleLogger)),
            networkLogBehavior = Behavior.NetworkLogBehavior(networkLoggers = listOf(BeagleOkHttpLogger))
        )
    )
    Beagle.set(
        header,
        ScreenCaptureToolboxModule(),
        DividerModule(),
        TextModule("Logs", TextModule.Type.SECTION_HEADER),
        NetworkLogListModule(), // Might require additional setup, see below
        LogListModule(),
    )
}
