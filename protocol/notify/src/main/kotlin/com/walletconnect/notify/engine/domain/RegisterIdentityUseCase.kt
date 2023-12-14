@file:JvmSynthetic

package com.walletconnect.notify.engine.domain

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.notify.common.Statement
import kotlinx.coroutines.supervisorScope

@Deprecated("Can be removed when the old registration flow is no longer supported.")
internal class RegisterIdentityUseCase(
    private val identitiesInteractor: IdentitiesInteractor,
    private val identityServerUrl: String,
) {
    suspend operator fun invoke(
        accountId: AccountId, domain: String, isLimited: Boolean, onSign: (String) -> Cacao.Signature?, onSuccess: suspend (PublicKey) -> Unit, onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        identitiesInteractor
            .registerIdentity(accountId, Statement.fromBoolean(allApps = !isLimited).content, domain, listOf(identityServerUrl), identityServerUrl, onSign)
            .fold(onFailure = onFailure, onSuccess = { identityPublicKey -> onSuccess(identityPublicKey) })
    }
}
