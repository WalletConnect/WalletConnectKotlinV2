package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.engine.model.mapper.toSessionProposeRequest
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.supervisorScope

internal class RejectSessionUseCase(
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val proposalStorageRepository: ProposalStorageRepository,
) : RejectSessionUseCaseInterface {

    override suspend fun reject(proposerPublicKey: String, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val proposal = proposalStorageRepository.getProposalByKey(proposerPublicKey)
        proposalStorageRepository.deleteProposal(proposerPublicKey)
        verifyContextStorageRepository.delete(proposal.requestId)

        jsonRpcInteractor.respondWithError(
            proposal.toSessionProposeRequest(),
            PeerError.EIP1193.UserRejectedRequest(reason),
            IrnParams(Tags.SESSION_PROPOSE_RESPONSE, Ttl(FIVE_MINUTES_IN_SECONDS)),
            onSuccess = { onSuccess() },
            onFailure = { error -> onFailure(error) })
    }
}

internal interface RejectSessionUseCaseInterface {
    suspend fun reject(proposerPublicKey: String, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit = {})
}