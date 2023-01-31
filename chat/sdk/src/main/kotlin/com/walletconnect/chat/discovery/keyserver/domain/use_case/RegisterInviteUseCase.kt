@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.chat.discovery.keyserver.data.client.KeyServerClient

internal class RegisterInviteUseCase(
    private val client: KeyServerClient,
) {
    suspend operator fun invoke(idAuth: String): Result<Unit> = runCatching {
        client.registerInvite(idAuth)
    }
}