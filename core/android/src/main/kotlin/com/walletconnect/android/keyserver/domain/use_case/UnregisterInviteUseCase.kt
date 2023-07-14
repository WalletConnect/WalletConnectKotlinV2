package com.walletconnect.android.keyserver.domain.use_case

import com.walletconnect.android.keyserver.data.service.KeyServerService
import com.walletconnect.android.keyserver.model.KeyServerBody

class UnregisterInviteUseCase(
    private val service: KeyServerService,
) {
    suspend operator fun invoke(idAuth: String): Result<Unit> = runCatching {
        service.unregisterInvite(KeyServerBody.UnregisterInvite(idAuth)).unwrapUnit()
    }
}