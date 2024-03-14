@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.internal.common.exception.AccountHasDifferentStatementStored
import com.walletconnect.android.internal.common.exception.AccountHasNoCacaoPayloadStored
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.MissingKeyException
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import com.walletconnect.notify.engine.domain.createAuthorizationReCaps

internal class IsRegisteredUseCase(
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val identitiesInteractor: IdentitiesInteractor,
    private val identityServerUrl: String,
) : IsRegisteredUseCaseInterface {

    override suspend fun isRegistered(account: String, domain: String, allApps: Boolean): Boolean {
        try {
            registeredAccountsRepository.getAccountByAccountId(account).let {
                return identitiesInteractor.getAlreadyRegisteredValidIdentity(
                    accountId = AccountId(account),
                    domain = domain,
                    resources = listOf(identityServerUrl, createAuthorizationReCaps())
                )
                    .map { true }
                    .recover { exception ->
                        when (exception) {
                            is MissingKeyException, is AccountHasNoCacaoPayloadStored, is AccountHasDifferentStatementStored -> false
                            else -> {
                                false
                            }
                        }
                    }.getOrElse { false }
            }
        } catch (_: NullPointerException) {
            return false
        }
    }
}

internal interface IsRegisteredUseCaseInterface {
    suspend fun isRegistered(account: String, domain: String, allApps: Boolean = false): Boolean
}