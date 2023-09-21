package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.MissingKeyException
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.getInviteTag
import com.walletconnect.android.internal.utils.getParticipantTag
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.android.keyserver.domain.use_case.UnregisterInviteUseCase
import com.walletconnect.chat.common.exceptions.InvalidAccountIdException
import com.walletconnect.chat.common.exceptions.InviteKeyNotFound
import com.walletconnect.chat.jwt.use_case.EncodeUnregisterInviteKeyDidJwtPayloadUseCase
import com.walletconnect.chat.storage.AccountsStorageRepository
import com.walletconnect.foundation.util.jwt.encodeX25519DidKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class GoPrivateUseCase(
    private val keyserverUrl: String,
    private val identitiesInteractor: IdentitiesInteractor,
    private val accountsRepository: AccountsStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val unregisterInviteUseCase: UnregisterInviteUseCase,
) : GoPrivateUseCaseInterface {

    override fun goPrivate(accountId: AccountId, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        if (accountId.isValid()) {
            try {
                val invitePublicKey = keyManagementRepository.getPublicKey(accountId.getInviteTag())
                val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(accountId)

                val didJwt = encodeDidJwt(
                    identityPrivateKey,
                    EncodeUnregisterInviteKeyDidJwtPayloadUseCase(encodeX25519DidKey(invitePublicKey.keyAsBytes), accountId),
                    EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
                ).getOrElse() { error -> return@goPrivate onError(error) }

                scope.launch {
                    supervisorScope {
                        unregisterInviteUseCase(didJwt.value).fold(
                            onSuccess = {
                                accountsRepository.removeAccountPublicInviteKey(accountId)
                                keyManagementRepository.removeKeys(accountId.getInviteTag())
                                val inviteTopic = keyManagementRepository.getTopicFromKey(invitePublicKey)
                                keyManagementRepository.removeKeys(inviteTopic.getParticipantTag())
                                jsonRpcInteractor.unsubscribe(inviteTopic)
                                onSuccess()
                            },
                            onFailure = { error -> onError(error) }
                        )
                    }
                }
            } catch (e: MissingKeyException) {
                onError(InviteKeyNotFound("Unable to find stored invite key for $accountId"))
            }

        } else {
            onError(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }
}

internal interface GoPrivateUseCaseInterface {
    fun goPrivate(accountId: AccountId, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
}