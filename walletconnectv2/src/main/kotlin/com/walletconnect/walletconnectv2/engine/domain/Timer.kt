package com.walletconnect.walletconnectv2.engine.domain

import com.walletconnect.walletconnectv2.core.exceptions.client.WalletConnectException
import com.walletconnect.walletconnectv2.util.Logger
import java.util.*
import kotlin.concurrent.timer

var timerCounter: Int = 0
private const val TIMER_INTERVAL: Long = 1000L

internal fun startTimer(timeout: Int, onFailure: (Throwable) -> Unit) =
    timer(period = TIMER_INTERVAL, action = { onTick(timeout, onFailure) })

internal fun TimerTask.onTick(timeout: Int, onFailure: (Throwable) -> Unit) {
    timerCounter++
    Logger.error("Kobe; Tick: $timerCounter")
    if (timerCounter == timeout) {
        timerCounter = 0
        cancel()
        Logger.error("Kobe; throwing the timeout error")
        onFailure(WalletConnectException.TimeoutException("Timeout exception, no response within: $timeout seconds"))
        return
    }
}

internal fun Timer.reset() {
    timerCounter = 0
    cancel()
}