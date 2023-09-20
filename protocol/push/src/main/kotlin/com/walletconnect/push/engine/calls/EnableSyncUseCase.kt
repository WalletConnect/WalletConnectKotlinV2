package com.walletconnect.push.engine.calls

import com.walletconnect.android.internal.common.signing.cacao.Cacao

internal class EnableSyncUseCase(
) : EnableSyncUseCaseInterface {

    override suspend fun enableSync(account: String, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        // Do nothing
    }
}

internal interface EnableSyncUseCaseInterface {
    suspend fun enableSync(account: String, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}