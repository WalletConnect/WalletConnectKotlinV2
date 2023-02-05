@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.chat.discovery.keyserver.data.service.KeyServerService
import com.walletconnect.chat.discovery.keyserver.model.KeyServerResponse

internal class ResolveIdentityUseCase(
    private val service: KeyServerService,
) {
    suspend operator fun invoke(identityKey: String): Result<KeyServerResponse.ResolveIdentity> = runCatching {
        service.resolveIdentity(identityKey).body()!!.value
    }
}