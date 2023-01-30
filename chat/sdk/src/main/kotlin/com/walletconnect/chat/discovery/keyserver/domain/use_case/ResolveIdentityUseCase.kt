@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.chat.discovery.keyserver.data.client.KeyServerClient
import com.walletconnect.chat.discovery.keyserver.model.KeyServerDTO

internal class ResolveIdentityUseCase(
    private val client: KeyServerClient,
) {
    suspend operator fun invoke(identityKey: String): Result<KeyServerDTO.ResolveIdentityResponse> = runCatching {
        client.resolveIdentity(identityKey)
    }
}