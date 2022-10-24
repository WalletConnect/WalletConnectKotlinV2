@file:JvmSynthetic

package com.walletconnect.android.pairing

internal sealed class PairingDO {

    data class PairingDelete(
        val topic: String,
        val reason: String,
    ): PairingDO()
}