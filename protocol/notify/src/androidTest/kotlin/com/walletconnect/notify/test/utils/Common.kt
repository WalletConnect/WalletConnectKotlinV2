package com.walletconnect.notify.test.utils

import com.walletconnect.android.Core
import com.walletconnect.notify.client.Notify
import junit.framework.TestCase.fail
import timber.log.Timber

internal fun globalOnError(error: Notify.Model.Error) {
    Timber.e("globalOnError: ${error.throwable.stackTraceToString()}")
    fail(error.throwable.message)
}

internal fun globalOnError(error: Core.Model.Error) {
    Timber.e("globalOnError: ${error.throwable.stackTraceToString()}")
    fail(error.throwable.message)
}