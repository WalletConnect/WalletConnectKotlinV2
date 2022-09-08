package com.walletconnect.android.impl.utils

import com.walletconnect.android.impl.BuildConfig
import timber.log.Timber

object Logger {
    const val TAG = "WalletConnectV2"

    init {
        if (BuildConfig.DEBUG) {
            // Source: https://medium.com/androiddevnotes/customize-your-android-timber-logger-setup-to-add-a-global-tag-and-a-method-name-to-the-logs-for-e7f23acd844f
            Timber.plant(
                object : Timber.DebugTree() {
                    /**
                     * Override [log] to modify the tag and add a "global tag" prefix to it. You can rename the String "global_tag_" as you see fit.
                     */
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                        super.log(priority, TAG, message, t)
                    }
                })
        }
    }

    fun log(logMsg: String?) {
        Timber.d(logMsg)
    }

    fun log(throwable: Throwable?) {
        Timber.d(throwable)
    }

    fun error(errorMsg: String?) {
        Timber.e(errorMsg)
    }

    fun error(throwable: Throwable?) {
        Timber.e(throwable)
    }
}