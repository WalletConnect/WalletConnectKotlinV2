package com.walletconnect.android.internal.pairing

import com.walletconnect.android.common.model.Error

sealed class Uncategorized: Error {

    data class NoMatchingTopic(val sequence: String, val topic: String) : Uncategorized() {
        override val message: String = "No matching $sequence with topic: $topic"
        override val code: Int = 1301
    }
}