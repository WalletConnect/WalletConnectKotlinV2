package com.walletconnect.push.engine.calls

import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.push.common.PeerError
import com.walletconnect.push.common.data.storage.ProposalStorageRepository
import kotlinx.coroutines.supervisorScope

internal class RejectUseCase(
    private val proposalStorageRepository: ProposalStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
): RejectUseCaseInterface {

    override suspend fun reject(proposalRequestId: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val proposalWithoutMetadata = proposalStorageRepository.getProposalByRequestId(proposalRequestId) ?: return@supervisorScope onFailure(IllegalArgumentException("Invalid proposal request id $proposalRequestId"))
            val responseTopic = Topic(sha256(proposalWithoutMetadata.dappPublicKey.keyAsBytes))
            val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

            jsonRpcInteractor.respondWithError(proposalWithoutMetadata.requestId, responseTopic, PeerError.Rejected.UserRejected(reason), irnParams) { error ->
                return@respondWithError onFailure(error)
            }

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}

internal interface RejectUseCaseInterface {
    suspend fun reject(proposalRequestId: Long, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}