package com.walletconnect.android.test.utils

import com.walletconnect.android.Core
import org.junit.jupiter.api.fail
import timber.log.Timber


internal fun globalOnError(error: Core.Model.Error) {
    Timber.e("globalOnError: ${error.throwable.stackTraceToString()}")
    fail(error.throwable)
}


internal fun globalOnError(error: Throwable) {
    Timber.e("globalOnError: ${error.stackTraceToString()}")
    fail(error)
}

