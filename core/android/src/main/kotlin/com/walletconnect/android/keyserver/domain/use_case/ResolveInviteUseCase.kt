package com.walletconnect.android.keyserver.domain.use_case

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.keyserver.data.service.KeyServerService
import com.walletconnect.android.keyserver.model.KeyServerResponse

class ResolveInviteUseCase(
    private val service: KeyServerService
) {
    suspend operator fun invoke(accountId: AccountId): Result<KeyServerResponse.ResolveInvite> = runCatching {
        service.resolveInvite(accountId.value).unwrapValue()
    }
}