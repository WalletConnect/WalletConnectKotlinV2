package com.walletconnect.android.internal.common

import com.tinder.scarlet.retry.BackoffStrategy
import kotlin.math.pow

class ConditionalExponentialBackoffStrategy(
    private val initialDurationMillis: Long,
    private val maxDurationMillis: Long
) : BackoffStrategy {
    init {
        require(initialDurationMillis > 0) { "initialDurationMillis, $initialDurationMillis, must be positive" }
        require(maxDurationMillis > 0) { "maxDurationMillis, $maxDurationMillis, must be positive" }
    }

    override var shouldBackoff: Boolean = false

    fun shouldBackoff(shouldBackoff: Boolean) {
        this.shouldBackoff = shouldBackoff
    }

    override fun backoffDurationMillisAt(retryCount: Int): Long =
        maxDurationMillis.toDouble().coerceAtMost(initialDurationMillis.toDouble() * 2.0.pow(retryCount.toDouble())).toLong()
}