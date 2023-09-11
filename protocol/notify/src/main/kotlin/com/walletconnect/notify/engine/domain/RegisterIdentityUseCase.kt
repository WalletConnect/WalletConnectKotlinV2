@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.foundation.common.model.PublicKey
import kotlinx.coroutines.supervisorScope

internal class RegisterIdentityUseCase(
    private val identitiesInteractor: IdentitiesInteractor,
    private val identityServerUrl: String,
) {
    suspend operator fun invoke(
        accountId: AccountId, isLimited: Boolean, domain: String, onSign: (String) -> Cacao.Signature?, onSuccess: suspend (PublicKey) -> Unit, onFailure: (Throwable) -> Unit,
    ) = supervisorScope {

        val statement = if (isLimited) limitedStatement() else unlimitedStatement()

        identitiesInteractor.registerIdentity(accountId, statement, domain, listOf(identityServerUrl), onSign).fold(
            onFailure = onFailure,
            onSuccess = { identityPublicKey -> onSuccess(identityPublicKey) },
        )
    }

    private fun limitedStatement() = "I further authorize this DAPP to send and receive messages on my behalf for this domain using my WalletConnect identity."
    private fun unlimitedStatement() = "I further authorize this WALLET to send and receive messages on my behalf for ALL domains using my WalletConnect identity."
}
