@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.chat.discovery.keyserver.data.service.KeyServerService
import com.walletconnect.chat.discovery.keyserver.model.KeyServerBody

internal class UnregisterIdentityUseCase(
    private val service: KeyServerService,
) {
    suspend operator fun invoke(cacao: Cacao): Result<Unit> = runCatching {
        service.unregisterIdentity(KeyServerBody.UnregisterIdentity(cacao)).unwrapUnit()
    }
}