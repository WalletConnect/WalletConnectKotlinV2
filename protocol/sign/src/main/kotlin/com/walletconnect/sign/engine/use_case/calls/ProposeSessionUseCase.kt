package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Pairing
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.PROPOSAL_EXPIRY
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.InvalidNamespaceException
import com.walletconnect.sign.common.exceptions.InvalidPropertiesException
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toNamespacesVOOptional
import com.walletconnect.sign.engine.model.mapper.toNamespacesVORequired
import com.walletconnect.sign.engine.model.mapper.toSessionProposeParams
import com.walletconnect.sign.engine.model.mapper.toVO
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.supervisorScope

internal class ProposeSessionUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val crypto: KeyManagementRepository,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val selfAppMetaData: AppMetaData,
    private val logger: Logger
) : ProposeSessionUseCaseInterface {

    override suspend fun proposeSession(
        requiredNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        optionalNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        properties: Map<String, String>?,
        pairing: Pairing,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        val relay = RelayProtocolOptions(pairing.relayProtocol, pairing.relayData)

        runCatching { validate(requiredNamespaces, optionalNamespaces, properties) }.fold(
            onSuccess = {
                val expiry = Expiry(PROPOSAL_EXPIRY)
                val selfPublicKey: PublicKey = crypto.generateAndStoreX25519KeyPair()
                val sessionProposal: SignParams.SessionProposeParams =
                    toSessionProposeParams(
                        listOf(relay),
                        requiredNamespaces ?: emptyMap(),
                        optionalNamespaces ?: emptyMap(),
                        properties, selfPublicKey, selfAppMetaData, expiry
                    )
                val request = SignRpc.SessionPropose(params = sessionProposal)
                proposalStorageRepository.insertProposal(sessionProposal.toVO(pairing.topic, request.id))
                val irnParams = IrnParams(Tags.SESSION_PROPOSE, Ttl(fiveMinutesInSeconds), true)
                jsonRpcInteractor.subscribe(pairing.topic) { error -> onFailure(error) }

                logger.log("Sending proposal on topic: ${pairing.topic.value}")
                jsonRpcInteractor.publishJsonRpcRequest(pairing.topic, irnParams, request,
                    onSuccess = {
                        logger.log("Session proposal sent successfully, topic: ${pairing.topic}")
                        onSuccess()
                    },
                    onFailure = { error ->
                        logger.error("Failed to send a session proposal: $error")
                        onFailure(error)
                    }
                )
            },
            onFailure = { error ->
                logger.error("Failed to validate session proposal: $error")
                onFailure(error)
            }
        )
    }

    private fun validate(
        requiredNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        optionalNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        properties: Map<String, String>?
    ) {
        requiredNamespaces?.let { namespaces ->
            SignValidator.validateProposalNamespaces(namespaces.toNamespacesVORequired()) { error ->
                logger.error("Failed to send a session proposal - required namespaces error: $error")
                throw InvalidNamespaceException(error.message)
            }
        }

        optionalNamespaces?.let { namespaces ->
            SignValidator.validateProposalNamespaces(namespaces.toNamespacesVOOptional()) { error ->
                logger.error("Failed to send a session proposal - optional namespaces error: $error")
                throw InvalidNamespaceException(error.message)
            }
        }

        properties?.let {
            SignValidator.validateProperties(properties) { error ->
                logger.error("Failed to send a session proposal - session properties error: $error")
                throw InvalidPropertiesException(error.message)
            }
        }
    }
}

internal interface ProposeSessionUseCaseInterface {
    suspend fun proposeSession(
        requiredNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        optionalNamespaces: Map<String, EngineDO.Namespace.Proposal>?,
        properties: Map<String, String>?,
        pairing: Pairing,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}