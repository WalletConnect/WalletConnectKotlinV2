package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.common.exceptions.AccountsAlreadyHaveInviteException
import com.walletconnect.chat.common.exceptions.AccountsAlreadyHaveThreadException
import com.walletconnect.chat.common.exceptions.InviteMessageTooLongException
import com.walletconnect.chat.common.json_rpc.ChatParams
import com.walletconnect.chat.common.json_rpc.ChatRpc
import com.walletconnect.chat.common.model.Contact
import com.walletconnect.chat.common.model.Invite
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.common.model.SendInvite
import com.walletconnect.chat.engine.domain.ChatValidator
import com.walletconnect.chat.engine.sync.use_case.requests.SetSentInviteToChatSentInvitesStoreUseCase
import com.walletconnect.chat.jwt.use_case.EncodeInviteProposalDidJwtPayloadUseCase
import com.walletconnect.chat.storage.ContactStorageRepository
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeX25519DidKey
import com.walletconnect.util.generateId
import com.walletconnect.utils.extractTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


internal class SendInviteUseCase(
    private val keyserverUrl: String,
    private val logger: Logger,
    private val invitesRepository: InvitesStorageRepository,
    private val threadsRepository: ThreadsStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val identitiesInteractor: IdentitiesInteractor,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val contactRepository: ContactStorageRepository,
    private val setSentInviteToChatSentInvitesStoreUseCase: SetSentInviteToChatSentInvitesStoreUseCase,
) : SendInviteUseCaseInterface {

    override suspend fun invite(invite: SendInvite, onSuccess: (Long) -> Unit, onError: (Throwable) -> Unit) {
        if (!ChatValidator.isInviteMessageValid(invite.message.value)) {
            return onError(InviteMessageTooLongException())
        }

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        if (withContext(scope.coroutineContext) { invitesRepository.checkIfAccountsHaveExistingInvite(invite.inviterAccount.value, invite.inviteeAccount.value) }) {
            return onError(AccountsAlreadyHaveInviteException)
        }

        if (runBlocking(scope.coroutineContext) { threadsRepository.checkIfSelfAccountHaveThreadWithPeerAccount(invite.inviterAccount.value, invite.inviteeAccount.value) }) {
            return onError(AccountsAlreadyHaveThreadException)
        }

        if (runBlocking(scope.coroutineContext) { threadsRepository.checkIfSelfAccountHaveThreadWithPeerAccount(invite.inviteeAccount.value, invite.inviterAccount.value) }) {
            return onError(AccountsAlreadyHaveThreadException)
        }

        val decodedInviteePublicKey = decodeX25519DidKey(invite.inviteePublicKey)

        runCatching { withContext(scope.coroutineContext) { setContact(invite.inviteeAccount, decodedInviteePublicKey) } }.getOrElse { error -> return onError(error) }

        val inviterPublicKey = runCatching { keyManagementRepository.generateAndStoreX25519KeyPair() }.getOrElse { error -> return onError(error) }
        val inviterPrivateKey = keyManagementRepository.getKeyPair(inviterPublicKey).second

        try {
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviterPublicKey, decodedInviteePublicKey)
            val inviteTopic = keyManagementRepository.getTopicFromKey(decodedInviteePublicKey)
            keyManagementRepository.setKeyAgreement(inviteTopic, inviterPublicKey, decodedInviteePublicKey)

            val participants = Participants(senderPublicKey = inviterPublicKey, receiverPublicKey = decodedInviteePublicKey)
            val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(invite.inviterAccount)

            val didJwt = encodeDidJwt(
                identityPrivateKey,
                EncodeInviteProposalDidJwtPayloadUseCase(inviterPublicKey, invite.inviteeAccount, invite.message.value),
                EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
            ).getOrElse() { error -> return@invite onError(error) }


            val inviteParams = ChatParams.InviteParams(inviteAuth = didJwt.value)
            val inviteId = generateId()
            val payload = ChatRpc.ChatInvite(id = inviteId, params = inviteParams)
            val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)

            keyManagementRepository.setKey(symmetricKey, acceptTopic.value)
            jsonRpcInteractor.subscribe(acceptTopic) { error -> return@subscribe onError(error) }

            val irnParams = IrnParams(Tags.CHAT_INVITE, Ttl(MONTH_IN_SECONDS), true)
            jsonRpcInteractor.publishJsonRpcRequest(inviteTopic, irnParams, payload, EnvelopeType.ONE, participants,
                {
                    val sentInvite = Invite.Sent(
                        inviteId, invite.inviterAccount, invite.inviteeAccount, invite.message, inviterPublicKey,
                        InviteStatus.PENDING, acceptTopic, symmetricKey, inviterPrivateKey,
                        //todo: use publishedAt from relay https://github.com/WalletConnect/WalletConnectKotlinV2/issues/872
                        timestamp = inviteId.extractTimestamp()
                    )
                    scope.launch { invitesRepository.insertInvite(sentInvite) }
                    setSentInviteToChatSentInvitesStoreUseCase(sentInvite, onSuccess = {}, onError = onError)
                    onSuccess(inviteId)
                },
                { throwable ->
                    logger.error(throwable)
                    jsonRpcInteractor.unsubscribe(acceptTopic)
                    onError(throwable)
                }
            )
        } catch (error: Exception) {
            keyManagementRepository.removeKeys(inviterPublicKey.keyAsHex)
            onError(error)
        }
    }

    private suspend fun setContact(accountId: AccountId, publicInviteKey: PublicKey) {
        contactRepository.upsertContact(Contact(accountId, publicInviteKey, accountId.value))
    }


}

internal interface SendInviteUseCaseInterface {
    suspend fun invite(invite: SendInvite, onSuccess: (Long) -> Unit, onError: (Throwable) -> Unit)
}