package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.internal.utils.CoreValidator.isExpired
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.exceptions.SessionProposalExpiredException
import com.walletconnect.sign.engine.model.mapper.toSessionProposeRequest
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RejectSessionUseCase(
    private val verifyContextStorageRepository: VerifyContextStorageRepository,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val pairingController: PairingControllerInterface,
    private val logger: Logger
) : RejectSessionUseCaseInterface {

    override suspend fun reject(proposerPublicKey: String, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val proposal = proposalStorageRepository.getProposalByKey(proposerPublicKey)
        proposal.expiry?.let {
            if (it.isExpired()) {
                logger.error("Proposal expired on reject, topic: ${proposal.pairingTopic.value}, id: ${proposal.requestId}")
                throw SessionProposalExpiredException("Session proposal expired")
            }
        }

        logger.log("Sending session rejection, topic: ${proposal.pairingTopic.value}")
        jsonRpcInteractor.respondWithError(
            proposal.toSessionProposeRequest(),
            PeerError.EIP1193.UserRejectedRequest(reason),
            IrnParams(Tags.SESSION_PROPOSE_RESPONSE_REJECT, Ttl(fiveMinutesInSeconds)),
            onSuccess = {
                logger.log("Session rejection sent successfully, topic: ${proposal.pairingTopic.value}")
                scope.launch {
                    proposalStorageRepository.deleteProposal(proposerPublicKey)
                    verifyContextStorageRepository.delete(proposal.requestId)
                    pairingController.deleteAndUnsubscribePairing(Core.Params.Delete(proposal.pairingTopic.value))
                }
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Session rejection sent failure, topic: ${proposal.pairingTopic.value}. Error: $error")
                onFailure(error)
            })
    }
}

internal interface RejectSessionUseCaseInterface {
    suspend fun reject(proposerPublicKey: String, reason: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit = {})
}