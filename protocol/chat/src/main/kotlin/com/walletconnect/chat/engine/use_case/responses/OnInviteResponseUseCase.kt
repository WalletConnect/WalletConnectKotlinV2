package com.walletconnect.chat.engine.use_case.responses

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.WCResponse
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.keyserver.domain.IdentitiesInteractor
import com.walletconnect.chat.common.model.Events
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.chat.storage.ThreadsStorageRepository
import com.walletconnect.foundation.util.Logger
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
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
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
            _events.emit(Events.OnInviteRejected(sentInvite))
        }
    }

    private suspend fun onAccepted(response: JsonRpcResponse.JsonRpcResult, wcResponse: WCResponse) {
//
//        //TODO this method is called because ChatNotifyResponseAuthParams.ResponseAuth is to generic
//        //TODO remove return and handle this being called when it shouldn't
//        return
//
//        val acceptParams = response.result as ChatNotifyResponseAuthParams.ResponseAuth
//
//        // TODO
//        //  Discuss what state is invite in if not verified
//        //  invitesRepository.updateStatusByInviteId(wcResponse.response.id, InviteStatus.?????????)
//        val claims = extractVerifiedDidJwtClaims<ChatDidJwtClaims.InviteApproval>(acceptParams.responseAuth).getOrElse() { error -> return@onAccepted logger.error(error) }
//        if (claims.action != ChatDidJwtClaims.InviteApproval.ACT) return logger.error(InvalidActClaims(ChatDidJwtClaims.InviteApproval.ACT))
//
//        try {
//            val sentInvite = invitesRepository.getSentInviteByInviteId(wcResponse.response.id)
//
//            if (sentInvite.status == InviteStatus.APPROVED || sentInvite.status == InviteStatus.REJECTED) return logger.error(InviteResponseWasAlreadyReceived)
//
//            val selfPubKey: PublicKey = sentInvite.inviterPublicKey
//            val peerPubKey = decodeX25519DidKey(claims.subject)
//            val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(selfPubKey, peerPubKey)
//            val threadTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
//            keyManagementRepository.setKey(symmetricKey, threadTopic.value)
//
//            val inviteeAccountId = identitiesInteractor.resolveIdentityDidKey(claims.issuer).getOrThrow().value
//            val inviterAccountId = decodeDidPkh(claims.audience)
//            threadsRepository.insertThread(threadTopic.value, selfAccount = inviterAccountId, peerAccount = inviteeAccountId)
//
//            jsonRpcInteractor.subscribe(threadTopic) { error -> scope.launch { _events.emit(SDKError(error)) } }
//
//            invitesRepository.updateStatusByInviteId(wcResponse.response.id, InviteStatus.APPROVED)
//
//            _events.emit(Events.OnInviteAccepted(threadTopic.value, sentInvite))
//        } catch (e: Exception) {
//            scope.launch { _events.emit(SDKError(e)) }
//            return
//        }
    }

}