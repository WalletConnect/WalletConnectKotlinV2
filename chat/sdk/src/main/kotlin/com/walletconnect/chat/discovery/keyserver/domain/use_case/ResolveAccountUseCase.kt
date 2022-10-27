@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.common.model.AccountIdWithPublicKey
import com.walletconnect.chat.discovery.keyserver.data.client.KeyServerClient
import com.walletconnect.chat.discovery.keyserver.model.toVOAccount

internal class ResolveAccountUseCase(
    private val client: KeyServerClient,
) {
    suspend operator fun invoke(accountId: AccountId): Result<AccountIdWithPublicKey> = try {
        val accountVO = client.resolve(accountId.value).toVOAccount()
        Result.success(accountVO)
    } catch (e: Exception) {
        Result.failure(e)
    }
}