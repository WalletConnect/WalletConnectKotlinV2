package com.walletconnect.sign.util

object Expiration {
    val inactivePairing: Long = Time.currentTimeInSeconds + Time.fiveMinutesInSeconds
    val activePairing: Long = Time.currentTimeInSeconds + Time.monthInSeconds
    val activeSession: Long = Time.currentTimeInSeconds + Time.weekInSeconds
}