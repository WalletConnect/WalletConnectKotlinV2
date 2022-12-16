@file:JvmSynthetic

package com.walletconnect.push.dapp.engine.domain

import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.util.Logger

internal class DappEngine(
    private val pairingHandler: PairingControllerInterface,
    private val logger: Logger
) {

    init {
        pairingHandler.register("")
    }

    fun setup() {

    }
}