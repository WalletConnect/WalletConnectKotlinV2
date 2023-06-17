package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.getInviteTag
import com.walletconnect.android.internal.utils.getParticipantTag
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.storage.AccountsStorageRepository
import com.walletconnect.foundation.util.jwt.encodeEd25519DidKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class UnregisterIdentityUseCase(
    private val keyserverUrl: String,
    private val identitiesInteractor: IdentitiesInteractor,
    private val accountsRepository: AccountsStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) : UnregisterIdentityUseCaseInterface {

    override fun unregister(accountId: AccountId, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                identitiesInteractor.unregisterIdentity(accountId, keyserverUrl).fold(
                    onFailure = { error -> onError(error) },
                    onSuccess = { identityPublicKey ->
                        val account = accountsRepository.getAccountByAccountId(accountId)
                        if (account.publicInviteKey != null && account.inviteTopic != null) {
                            keyManagementRepository.removeKeys(accountId.getInviteTag())
                            keyManagementRepository.removeKeys(account.inviteTopic.getParticipantTag())
                            jsonRpcInteractor.unsubscribe(account.inviteTopic)
                        }
                        accountsRepository.deleteAccountByAccountId(accountId)
                        val didKey = encodeEd25519DidKey(identityPublicKey.keyAsBytes)
                        onSuccess(didKey)
                    }
                )
            }
        }
    }
}

internal interface UnregisterIdentityUseCaseInterface {
    fun unregister(accountId: AccountId, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit)
}