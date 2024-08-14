@file:JvmSynthetic

package com.walletconnect.android.pairing.engine.model

internal sealed class EngineDO {
    data class PairingState(
        val isPairingState: Boolean
    ) : EngineDO()
}