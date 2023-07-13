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
import com.walletconnect.android.keyserver.domain.use_case.RegisterInviteUseCase
import com.walletconnect.chat.common.exceptions.InvalidAccountIdException
import com.walletconnect.chat.engine.sync.use_case.requests.SetInviteKeyToChatInviteKeyStoreUseCase
import com.walletconnect.chat.jwt.use_case.EncodeRegisterInviteKeyDidJwtPayloadUseCase
import com.walletconnect.chat.storage.AccountsStorageRepository
import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeX25519DidKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class GoPublicUseCase(
    private val keyserverUrl: String,
    private val identitiesInteractor: IdentitiesInteractor,
    private val accountsRepository: AccountsStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val registerInviteUseCase: RegisterInviteUseCase,
    private val setInviteKeyToChatInviteKeyStoreUseCase: SetInviteKeyToChatInviteKeyStoreUseCase,
) : GoPublicUseCaseInterface {

    override fun goPublic(accountId: AccountId, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit) {
        fun onSuccess(invitePublicKey: PublicKey, invitePrivateKey: PrivateKey) {
            scope.launch {
                supervisorScope {
                    val inviteTopic = keyManagementRepository.getTopicFromKey(invitePublicKey)
                    keyManagementRepository.setKey(invitePublicKey, accountId.getInviteTag())
                    keyManagementRepository.setKey(invitePublicKey, inviteTopic.getParticipantTag())
                    accountsRepository.setAccountPublicInviteKey(accountId, invitePublicKey, inviteTopic)
                    jsonRpcInteractor.subscribe(inviteTopic)

                    setInviteKeyToChatInviteKeyStoreUseCase(accountId, invitePublicKey, invitePrivateKey, onSuccess = {}, onError = onError)
                    onSuccess(invitePublicKey.keyAsHex)
                }
            }
        }

        if (accountId.isValid()) {
            try {
                val storedPublicKey = keyManagementRepository.getPublicKey(accountId.getInviteTag())
                onSuccess(storedPublicKey.keyAsHex)
            } catch (e: MissingKeyException) {
                val (invitePublicKey, invitePrivateKey) = keyManagementRepository.generateAndStoreX25519KeyPair().let { invitePublicKey -> keyManagementRepository.getKeyPair(invitePublicKey) }

                val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(accountId)

                val didJwt = encodeDidJwt(
                    identityPrivateKey,
                    EncodeRegisterInviteKeyDidJwtPayloadUseCase(encodeX25519DidKey(invitePublicKey.keyAsBytes), accountId),
                    EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
                ).getOrElse() { error -> return@goPublic onError(error) }

                scope.launch {
                    supervisorScope {
                        registerInviteUseCase(didJwt.value).fold(
                            onSuccess = { onSuccess(invitePublicKey, invitePrivateKey) },
                            onFailure = { error -> onError(error) }
                        )
                    }
                }
            }
        } else {
            onError(InvalidAccountIdException("AccountId is not CAIP-10 complaint"))
        }
    }
}

internal interface GoPublicUseCaseInterface {
    fun goPublic(accountId: AccountId, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit)
}