package com.walletconnect.android.keyserver.domain.use_case

import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.android.keyserver.data.service.KeyServerService
import com.walletconnect.android.keyserver.model.KeyServerBody

class RegisterIdentityUseCase(
    private val service: KeyServerService,
) {
    suspend operator fun invoke(cacao: Cacao): Result<Unit> = runCatching {
        service.registerIdentity(KeyServerBody.RegisterIdentity(cacao)).unwrapUnit()
    }
}