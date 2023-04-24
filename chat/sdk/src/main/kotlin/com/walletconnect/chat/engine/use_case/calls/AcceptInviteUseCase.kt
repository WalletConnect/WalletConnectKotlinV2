package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.jwt.did.EncodeDidJwtPayloadUseCase
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.CoreChatParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.internal.utils.SELF_INVITE_PUBLIC_KEY_CONTEXT
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.common.exceptions.MissingInviteRequestException
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.json_rpc.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.chat.jwt.use_case.EncodeInviteApprovalDidJwtPayloadUseCase
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.launch


internal class AcceptInviteUseCase(
    private val keyserverUrl: String,
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val logger: Logger,
    private val invitesRepository: InvitesStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val identitiesInteractor: IdentitiesInteractor,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val threadsRepository: ThreadsStorageRepository,
) : AcceptInviteUseCaseInterface {
    private fun AccountId.getInviteTag(): String = "$SELF_INVITE_PUBLIC_KEY_CONTEXT${this.value}"

    override fun accept(inviteId: Long, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        scope.launch {
            try {
                val jsonRpcHistoryEntry = getPendingJsonRpcHistoryEntryByIdUseCase(inviteId)

                if (jsonRpcHistoryEntry == null) {
                    logger.error(MissingInviteRequestException.message)
                    return@launch onFailure(MissingInviteRequestException)
                }

                val invite = invitesRepository.getReceivedInviteByInviteId(inviteId)
                val inviterPublicKey = invite.inviterPublicKey
                val inviteeAccountId = invite.inviteeAccount
                val inviterAccountId = invite.inviterAccount

                val inviteePublicKey = keyManagementRepository.getPublicKey(inviteeAccountId.getInviteTag())
                val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviteePublicKey, inviterPublicKey)
                val acceptTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
                keyManagementRepository.setKey(symmetricKey, acceptTopic.value)

                val publicKey = keyManagementRepository.generateAndStoreX25519KeyPair()
                val (identityPublicKey, identityPrivateKey) = identitiesInteractor.getIdentityKeyPair(inviteeAccountId)

                val didJwt = encodeDidJwt(
                    identityPrivateKey,
                    EncodeInviteApprovalDidJwtPayloadUseCase(publicKey, inviterAccountId),
                    EncodeDidJwtPayloadUseCase.Params(identityPublicKey, keyserverUrl)
                ).getOrElse() { error ->
                    onFailure(error)
                    return@launch
                }

                val acceptanceParams = CoreChatParams.AcceptanceParams(responseAuth = didJwt.value)
                val responseParams = JsonRpcResponse.JsonRpcResult(jsonRpcHistoryEntry.id, result = acceptanceParams)
                val irnParams = IrnParams(Tags.CHAT_INVITE_RESPONSE, Ttl(MONTH_IN_SECONDS))
                jsonRpcInteractor.publishJsonRpcResponse(acceptTopic, irnParams, responseParams, {}, { error -> return@publishJsonRpcResponse onFailure(error) })

                val threadSymmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(publicKey, inviterPublicKey)
                val threadTopic = keyManagementRepository.getTopicFromKey(threadSymmetricKey)
                keyManagementRepository.setKey(threadSymmetricKey, threadTopic.value)

                threadsRepository.insertThread(threadTopic.value, selfAccount = inviteeAccountId.value, peerAccount = inviterAccountId.value)
                invitesRepository.updateStatusByInviteId(inviteId, InviteStatus.APPROVED)

                jsonRpcInteractor.subscribe(threadTopic) { error -> return@subscribe onFailure(error) }
                onSuccess(threadTopic.value)

            } catch (error: Exception) {
                onFailure(error)
            }
        }
    }
}

internal interface AcceptInviteUseCaseInterface {
    fun accept(inviteId: Long, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit)
}