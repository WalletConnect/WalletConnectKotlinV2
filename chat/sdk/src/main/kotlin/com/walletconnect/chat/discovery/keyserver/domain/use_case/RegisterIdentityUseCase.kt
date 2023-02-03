@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.android.internal.common.cacao.Cacao
import com.walletconnect.chat.discovery.keyserver.data.client.KeyServerClient

internal class RegisterIdentityUseCase(
    private val client: KeyServerClient,
) {
    suspend operator fun invoke(cacao: Cacao): Result<Unit> = runCatching {
        client.registerIdentity(cacao)
    }
}