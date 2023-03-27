package com.walletconnect.android.internal.common.model

import java.util.concurrent.TimeUnit

data class NetworkClientTimeout(
    val timeout: Long,
    val timeUnit: TimeUnit
) {

    companion object {
        fun getDefaultTimeout() = NetworkClientTimeout(
            timeout = 5000L,
            timeUnit = TimeUnit.MILLISECONDS
        )
    }
}
