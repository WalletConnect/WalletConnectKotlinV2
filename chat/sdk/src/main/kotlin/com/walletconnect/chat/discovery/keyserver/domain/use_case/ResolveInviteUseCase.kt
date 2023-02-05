@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.domain.use_case

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.discovery.keyserver.data.service.KeyServerService
import com.walletconnect.chat.discovery.keyserver.model.KeyServerResponse

internal class ResolveInviteUseCase(
    private val service: KeyServerService
) {
    suspend operator fun invoke(accountId: AccountId): Result<KeyServerResponse.ResolveInvite> = runCatching {
        service.resolveInvite(accountId.value).body()!!.value
    }
}