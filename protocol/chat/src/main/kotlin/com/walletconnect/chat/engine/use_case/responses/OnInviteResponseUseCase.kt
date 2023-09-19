package com.walletconnect.chat.engine.use_case.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.common.exceptions.InvalidActClaims
import com.walletconnect.chat.common.exceptions.InviteResponseWasAlreadyReceived
import com.walletconnect.chat.common.model.Events
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.common.model.Thread
import com.walletconnect.chat.engine.sync.use_case.requests.SetSentInviteToChatSentInvitesStoreUseCase
import com.walletconnect.chat.engine.sync.use_case.requests.SetThreadWithSymmetricKeyToChatThreadsStoreUseCase
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeDidPkh
import com.walletconnect.foundation.util.jwt.decodeX25519DidKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class OnInviteResponseUseCase(
    private val logger: Logger,
    private val invitesRepository: InvitesStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val identitiesInteractor: IdentitiesInteractor,
    private val threadsRepository: ThreadsStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val setSentInviteToChatSentInvitesStoreUseCase: SetSentInviteToChatSentInvitesStoreUseCase,
    private val setThreadWithSymmetricKeyToChatThreadsStoreUseCase: SetThreadWithSymmetricKeyToChatThreadsStoreUseCase,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse) {
        when (val response = wcResponse.response) {
            is JsonRpcResponse.JsonRpcError -> onRejected(wcResponse)
            is JsonRpcResponse.JsonRpcResult -> onAccepted(response, wcResponse)
        }
    }

    private suspend fun onRejected(wcResponse: WCResponse) {
        scope.launch {
            invitesRepository.updateStatusByInviteId(wcResponse.response.id, InviteStatus.REJECTED)
            val sentInvite = invitesRepository.getSentInviteByInviteId(wcResponse.response.id)
            setSentInviteToChatSentInvitesStoreUseCase(sentInvite.copy(status = InviteStatus.REJECTED), onSuccess = {}, onError = { error -> scope.launch { _events.emit(SDKError(error)) } })
            _events.emit(Events.OnInviteRejected(sentInvite))
        }
    }

    private suspend fun onAccepted(response: JsonRpcResponse.JsonRpcResult, wcResponse: WCResponse) {

        //TODO this method is called because ChatNotifyResponseAuthParams.ResponseAuth is to generic
        //TODO remove return and handle this being called when it shouldn't
        return

        val acceptParams = response.result as ChatNotifyResponseAuthParams.ResponseAuth

        // TODO
        //  Discuss what state is invite in if not verified
        //  invitesRepository.updateStatusByInviteId(wcResponse.response.id, InviteStatus.?????????)
        val claims = extractVerifiedDidJwtClaims<ChatDidJwtClaims.InviteApproval>(acceptParams.responseAuth).getOrElse() { error -> return@onAccepted logger.error(error) }
        if (claims.action != ChatDidJwtClaims.InviteApproval.ACT) return logger.error(InvalidActClaims(ChatDidJwtClaims.InviteApproval.ACT))

        try {
            val sentInvite = invitesRepository.getSentInviteByInviteId(wcResponse.response.id)

            if (sentInvite.status == InviteStatus.APPROVED || sentInvite.status == InviteStatus.REJECTED) return logger.error(InviteResponseWasAlreadyReceived)

            val selfPubKey: PublicKey = sentInvite.inviterPublicKey
            val peerPubKey = decodeX25519DidKey(claims.subject)
            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(selfPubKey, peerPubKey)
            val threadTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
            keyManagementRepository.setKey(symmetricKey, threadTopic.value)

            val inviteeAccountId = identitiesInteractor.resolveIdentityDidKey(claims.issuer).getOrThrow().value
            val inviterAccountId = decodeDidPkh(claims.audience)
            threadsRepository.insertThread(threadTopic.value, selfAccount = inviterAccountId, peerAccount = inviteeAccountId)

            jsonRpcInteractor.subscribe(threadTopic) { error -> scope.launch { _events.emit(SDKError(error)) } }

            invitesRepository.updateStatusByInviteId(wcResponse.response.id, InviteStatus.APPROVED)

            setSentInviteToChatSentInvitesStoreUseCase(sentInvite.copy(status = InviteStatus.APPROVED), onSuccess = {}, onError = { error -> scope.launch { _events.emit(SDKError(error)) } })

            setThreadWithSymmetricKeyToChatThreadsStoreUseCase(
                Thread(threadTopic, selfAccount = AccountId(inviterAccountId), peerAccount = AccountId(inviteeAccountId)), symmetricKey,
                onSuccess = {},
                onError = { error -> scope.launch { _events.emit(SDKError(error)) } }
            )

            _events.emit(Events.OnInviteAccepted(threadTopic.value, sentInvite))
        } catch (e: Exception) {
            scope.launch { _events.emit(SDKError(e)) }
            return
        }
    }

}