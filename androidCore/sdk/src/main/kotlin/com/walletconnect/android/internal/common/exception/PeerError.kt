package com.walletconnect.android.internal.common.exception

import com.walletconnect.android.internal.common.model.type.Error

sealed class Uncategorized : Error {

    data class NoMatchingTopic(val sequence: String, val topic: String) : Uncategorized() {
        override val message: String = "No matching $sequence with topic: $topic"
        override val code: Int = 1301
    }

    data class GenericError(val error: String) : Uncategorized() {
        override val message: String = "Generic error: $error"
        override val code: Int = 1302
    }
}

sealed class Invalid : Error {

    data class MethodUnsupported(val method: String) : Invalid() {
        override val message: String = "Unsupported Method Requested: $method"
        override val code: Int = 10001
    }

    object RequestExpired: Invalid() {
        override val message: String = "Request expired or expiry param validation failed (MIN_INTERVAL: 300, MAX_INTERVAL: 604800)"
        override val code: Int = 8000

    }
}

sealed class Reason : Error {

    object UserDisconnected : Reason() {
        override val message: String = DISCONNECT_MESSAGE
        override val code: Int = 6000
    }
}