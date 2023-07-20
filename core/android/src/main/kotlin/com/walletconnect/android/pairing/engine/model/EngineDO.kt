@file:JvmSynthetic

package com.walletconnect.android.pairing.engine.model

internal sealed class EngineDO {

    data class PairingDelete(
        val topic: String,
        val reason: String,
    ) : EngineDO()
}