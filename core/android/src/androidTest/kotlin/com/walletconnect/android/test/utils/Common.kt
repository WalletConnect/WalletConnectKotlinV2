package com.walletconnect.android.test.utils

import com.walletconnect.android.Core
import junit.framework.TestCase.fail
import timber.log.Timber

internal fun globalOnError(error: Core.Model.Error) {
    Timber.e("globalOnError: ${error.throwable.stackTraceToString()}")
    fail(error.throwable.message)
}

internal fun globalOnError(error: Throwable) {
    Timber.e("globalOnError: ${error.stackTraceToString()}")
    fail(error.message)
}