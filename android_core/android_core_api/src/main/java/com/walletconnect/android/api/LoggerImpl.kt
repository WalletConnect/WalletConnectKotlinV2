package com.walletconnect.android.api

import com.walletconnect.foundation.util.Logger
import timber.log.Timber

class LoggerImpl : Logger {

    override fun log(logMsg: String?) {
        Timber.d(logMsg)
    }

    override fun log(throwable: Throwable?) {
        Timber.d(throwable)
    }

    override fun error(errorMsg: String?) {
        Timber.e(errorMsg)
    }

    override fun error(throwable: Throwable?) {
        Timber.e(throwable)
    }
}