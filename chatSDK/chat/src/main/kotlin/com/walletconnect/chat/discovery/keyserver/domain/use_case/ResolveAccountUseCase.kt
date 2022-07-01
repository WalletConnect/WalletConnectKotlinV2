@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.chat.core.model.vo.AccountIdVO
import com.walletconnect.chat.core.model.vo.AccountIdWithPublicKeyVO
import com.walletconnect.chat.discovery.keyserver.data.client.KeyServerClient
import com.walletconnect.chat.discovery.keyserver.model.toVOAccount

internal class ResolveAccountUseCase(
    private val client: KeyServerClient,
) {
    suspend operator fun invoke(accountId: AccountIdVO): Result<AccountIdWithPublicKeyVO> = try {
        val accountVO = client.resolve(accountId.value).toVOAccount()
        Result.success(accountVO)
    } catch (e: Exception) {
        Result.failure(e)

    }
}