package com.walletconnect.sample.wallet

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
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.wcKoinApp
import okhttp3.Interceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber

fun initBeagle(app: Web3WalletApplication) {
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
        HeaderModule(
            title = app.getString(R.string.app_name),
            subtitle = BuildConfig.APPLICATION_ID,
            text = "${BuildConfig.BUILD_TYPE} v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        ),
        ScreenCaptureToolboxModule(),
        DividerModule(),
        TextModule("Logs", TextModule.Type.SECTION_HEADER),
        NetworkLogListModule(), // Might require additional setup, see below
        LogListModule(),
    )
}
