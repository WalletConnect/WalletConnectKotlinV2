@file:JvmSynthetic

package com.walletconnect.android.pairing.engine.model

import com.walletconnect.android.internal.common.model.Pairing

internal sealed class EngineDO {

    data class PairingDelete(
        val topic: String,
        val reason: String,
    ) : EngineDO()

    data class PairingExpire(
        val pairing: Pairing
    ) : EngineDO()

    data class PairingState(
        val isPairingState: Boolean
    ) : EngineDO()
}