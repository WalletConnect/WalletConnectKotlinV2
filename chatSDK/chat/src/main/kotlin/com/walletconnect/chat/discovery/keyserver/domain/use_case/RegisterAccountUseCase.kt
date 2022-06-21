@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO
import com.walletconnect.chat.discovery.keyserver.data.client.KeyServerClient
import com.walletconnect.chat.discovery.keyserver.model.toDTOAccount

internal class RegisterAccountUseCase(
    private val client: KeyServerClient,
) {
    suspend operator fun invoke(accountIdWithPublicKeyVO: AccountIdWithPublicKeyVO): Result<Unit> = try {
        client.register(accountIdWithPublicKeyVO.toDTOAccount())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

}