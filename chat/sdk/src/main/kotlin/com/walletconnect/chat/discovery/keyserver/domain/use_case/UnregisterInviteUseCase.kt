@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.chat.discovery.keyserver.data.service.KeyServerService
import com.walletconnect.chat.discovery.keyserver.model.KeyServerBody

internal class UnregisterInviteUseCase(
    private val service: KeyServerService,
) {
    suspend operator fun invoke(idAuth: String): Result<Unit> = runCatching {
        service.unregisterInvite(KeyServerBody.UnregisterInvite(idAuth)).unwrapUnit()
    }
}