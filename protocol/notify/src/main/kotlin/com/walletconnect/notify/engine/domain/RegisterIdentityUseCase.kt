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
        accountId: AccountId, domain: String, onSign: (String) -> Cacao.Signature?, onSuccess: suspend (PublicKey) -> Unit, onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        identitiesInteractor.registerIdentity(accountId, STATEMENT, domain, listOf(identityServerUrl), identityServerUrl, onSign).fold(
            onFailure = onFailure,
            onSuccess = { identityPublicKey -> onSuccess(identityPublicKey) },
        )
    }

    companion object {
        private const val STATEMENT = "I further authorize this app to send and receive messages on my behalf using my WalletConnect identity. Read more at https://walletconnect.com/identity"
    }
}
