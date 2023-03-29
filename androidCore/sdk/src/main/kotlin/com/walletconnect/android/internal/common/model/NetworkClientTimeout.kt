package com.walletconnect.android.internal.common.model

import java.util.concurrent.TimeUnit

data class NetworkClientTimeout(
    val timeout: Long,
    val timeUnit: TimeUnit
) {

    init {
        require(isTimeoutInRequiredRange()) {
            "Timeout must be in range of $MIN_TIMEOUT_LIMIT_AS_MILLIS .. $MAX_TIMEOUT_LIMIT_AS_MILLIS milliseconds"
        }
    }

    private fun isTimeoutInRequiredRange(): Boolean {
        val timeoutAsMillis = TimeUnit.MILLISECONDS.convert(timeout, timeUnit)
        return timeoutAsMillis in MIN_TIMEOUT_LIMIT_AS_MILLIS..MAX_TIMEOUT_LIMIT_AS_MILLIS
    }

    companion object {

        private const val MIN_TIMEOUT_LIMIT_AS_MILLIS = 5_000L
        private const val MAX_TIMEOUT_LIMIT_AS_MILLIS = 60_000L

        fun getDefaultTimeout() = NetworkClientTimeout(
            timeout = MIN_TIMEOUT_LIMIT_AS_MILLIS,
            timeUnit = TimeUnit.MILLISECONDS
        )
    }
}
