package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.common.model.Account
import com.walletconnect.chat.engine.sync.use_case.SetupSyncInChatUseCase
import com.walletconnect.chat.storage.AccountsStorageRepository
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RegisterIdentityUseCase(
    private val keyserverUrl: String,
    private val identitiesInteractor: IdentitiesInteractor,
    private val accountsRepository: AccountsStorageRepository,
    private val goPublicUseCase: GoPublicUseCase,
    private val setupSyncInChatUseCase: SetupSyncInChatUseCase,
) : RegisterIdentityUseCaseInterface {

    override fun register(accountId: AccountId, onSign: (String) -> Cacao.Signature?, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit, private: Boolean) {
        scope.launch {
            supervisorScope {
                identitiesInteractor.registerIdentity(accountId, keyserverUrl, onSign).fold(
                    onFailure = { error -> onError(error) },
                    onSuccess = { identityPublicKey ->
                        accountsRepository.upsertAccount(Account(accountId, identityPublicKey, null, null))
                        val didKey = encodeEd25519DidKey(identityPublicKey.keyAsBytes)
                        setupSyncInChatUseCase(accountId, onSign, onError = onError, onSuccess = {
                            if (!private) {
                                goPublicUseCase.goPublic(accountId, onSuccess = { onSuccess(didKey) }, onError = { error -> onError(error) })
                            } else {
                                onSuccess(didKey)
                            }
                        })
                    }
                )
            }
        }
    }
}

internal interface RegisterIdentityUseCaseInterface {
    fun register(accountId: AccountId, onSign: (String) -> Cacao.Signature?, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit, private: Boolean)
}