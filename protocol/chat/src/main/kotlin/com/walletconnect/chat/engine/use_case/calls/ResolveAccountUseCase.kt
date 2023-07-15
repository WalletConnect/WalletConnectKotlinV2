package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.keyserver.domain.use_case.ResolveInviteUseCase
import com.walletconnect.chat.common.exceptions.InvalidAccountIdException
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ResolveAccountUseCase(
    private val resolveInviteUseCase: ResolveInviteUseCase,
) : ResolveAccountUseCaseInterface {

    override fun resolveAccount(accountId: AccountId, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        if (accountId.isValid()) {
            scope.launch {
                supervisorScope {
                    resolveInviteUseCase(accountId).fold(
                        onSuccess = { response -> onSuccess(response.inviteKey) },
                        onFailure = { error -> onError(error) }
                    )
                }
            }
        } else {
            onError(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }
}

internal interface ResolveAccountUseCaseInterface {
    fun resolveAccount(accountId: AccountId, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit)
}