package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.pairing.client.PairingInterface

internal class PairUseCase(private val pairingInterface: PairingInterface) : PairUseCaseInterface {

    override fun pair(uri: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        pairingInterface.pair(
            pair = Core.Params.Pair(uri),
            onSuccess = { onSuccess() },
            onError = { error -> onFailure(error.throwable) }
        )
    }
}

internal interface PairUseCaseInterface {
    fun pair(uri: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}