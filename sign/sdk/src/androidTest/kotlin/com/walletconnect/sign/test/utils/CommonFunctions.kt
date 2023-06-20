package com.walletconnect.sign.test.utils

import com.walletconnect.android.Core
import com.walletconnect.sign.client.Sign
import org.junit.jupiter.api.fail
import timber.log.Timber

internal fun globalOnError(error: Sign.Model.Error) {
    Timber.e("globalOnError: ${error.throwable.stackTraceToString()}")
    fail(error.throwable)
}

internal fun globalOnError(error: Core.Model.Error) {
    Timber.e("globalOnError: ${error.throwable.stackTraceToString()}")
    fail(error.throwable)
}