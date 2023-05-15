package com.walletconnect.chat.engine.use_case.calls

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.MissingKeyException
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.android.internal.utils.getInviteTag
import com.walletconnect.chat.common.exceptions.InviteWasAlreadyRespondedTo
import com.walletconnect.chat.common.exceptions.MissingInviteRequestException
import com.walletconnect.chat.common.exceptions.PeerError
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.json_rpc.GetPendingJsonRpcHistoryEntryByIdUseCase
import com.walletconnect.chat.storage.InvitesStorageRepository
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.launch


internal class RejectInviteUseCase(
    private val getPendingJsonRpcHistoryEntryByIdUseCase: GetPendingJsonRpcHistoryEntryByIdUseCase,
    private val logger: Logger,
    private val invitesRepository: InvitesStorageRepository,
    private val keyManagementRepository: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) : RejectInviteUseCaseInterface {
    override fun reject(inviteId: Long, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        scope.launch {
            try {
                val jsonRpcHistoryEntry = getPendingJsonRpcHistoryEntryByIdUseCase(inviteId)

                if (jsonRpcHistoryEntry == null) {
                    logger.error(MissingInviteRequestException)
                    return@launch onError(MissingInviteRequestException)
                }

                val invite = invitesRepository.getReceivedInviteByInviteId(inviteId)
                if (invite.status == InviteStatus.APPROVED || invite.status == InviteStatus.REJECTED) {
                    logger.error(InviteWasAlreadyRespondedTo)
                    return@launch onError(InviteWasAlreadyRespondedTo)
                }

                val inviterPublicKey = invite.inviterPublicKey
                val inviteeAccountId = invite.inviteeAccount

                val inviteePublicKey = keyManagementRepository.getPublicKey(inviteeAccountId.getInviteTag())
                val symmetricKey = keyManagementRepository.generateSymmetricKeyFromKeyAgreement(inviteePublicKey, inviterPublicKey)
                val rejectTopic = keyManagementRepository.getTopicFromKey(symmetricKey)
                keyManagementRepository.setKey(symmetricKey, rejectTopic.value)

                val irnParams = IrnParams(Tags.CHAT_INVITE_RESPONSE, Ttl(MONTH_IN_SECONDS))
                val peerError = PeerError.UserRejectedInvitation("Invitation rejected by a user")
                val responseParams = JsonRpcResponse.JsonRpcError(jsonRpcHistoryEntry.id, error = JsonRpcResponse.Error(peerError.code, peerError.message))
                jsonRpcInteractor.publishJsonRpcResponse(rejectTopic, irnParams, responseParams, {}, { error -> return@publishJsonRpcResponse onError(error) })
                invitesRepository.updateStatusByInviteId(inviteId, InviteStatus.REJECTED)
                onSuccess()
            } catch (e: MissingKeyException) {
                return@launch onError(e)
            }
        }
    }
}

internal interface RejectInviteUseCaseInterface {
    fun reject(inviteId: Long, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
}