package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.common.model.vo.*
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.relay.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.PairingParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.PreSettlementPairingVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.proposal.*
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingParticipantVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingStateVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params.SessionPermissionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.AppMetaDataVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.SessionProposedPermissionsVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle.DeletedSessionVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle.SessionNotificationVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle.SessionProposalVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle.SessionRequestVO
import org.json.JSONObject
import java.net.URI
import kotlin.time.Duration

internal fun String.toPairProposal(): PairingParamsVO.Proposal {
    val properUriString = if (contains("wc://")) this else replace("wc:", "wc://")
    val pairUri = URI(properUriString)
    val mapOfQueryParameters: Map<String, String> =
        pairUri.query.split("&")
            .associate { query -> query.substringBefore("=") to query.substringAfter("=") }
    val relay = JSONObject(mapOfQueryParameters["relay"] ?: "{}")
    val publicKey = mapOfQueryParameters["publicKey"] ?: ""
    val controller: Boolean = mapOfQueryParameters["controller"].toBoolean()
    val ttl: Long = Duration.days(30).inWholeSeconds

    return PairingParamsVO.Proposal(
        topic = TopicVO(pairUri.userInfo),
        relay = relay,
        pairingProposer = PairingProposerVO(publicKey, controller),
        pairingSignal = PairingSignalVO("uri", PairingSignalParamsVO(properUriString)),
        permissions = PairingProposedPermissionsVO(JsonRPCVO(listOf(JsonRpcMethod.WC_SESSION_PROPOSE))),
        ttl = TtlVO(ttl)
    )
}

internal fun PairingParamsVO.Proposal.toPairingSuccess(
    settleTopic: TopicVO,
    expiry: ExpiryVO,
    selfPublicKey: PublicKey
): PairingParamsVO.Success =
    PairingParamsVO.Success(
        settledTopic = settleTopic,
        relay = relay,
        responder = PairingParticipantVO(publicKey = selfPublicKey.keyAsHex),
        expiry = expiry,
        state = PairingStateVO(null)
    )

internal fun PairingParamsVO.Proposal.toApprove(
    id: Long,
    settleTopic: TopicVO,
    expiry: ExpiryVO,
    selfPublicKey: PublicKey
): PreSettlementPairingVO.Approve = PreSettlementPairingVO.Approve(id = id, params = this.toPairingSuccess(settleTopic, expiry, selfPublicKey))

internal fun EngineDO.AppMetaData.toClientSyncAppMetaData() =
    AppMetaDataVO(name, description, url, icons)

internal fun EngineDO.SessionPermissions.toSessionsPermissions(): SessionPermissionsVO =
    SessionPermissionsVO(
        blockchain?.chains?.let { chains -> SessionProposedPermissionsVO.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> SessionProposedPermissionsVO.JsonRpc(methods) }
    )

internal fun EngineDO.JsonRpcResponse.JsonRpcResult.toJsonRpcResponseVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result)

internal fun PairingParamsVO.PayloadParams.toEngineDOSessionProposal(): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name = this.request.params.proposer.metadata?.name!!,
        description = this.request.params.proposer.metadata.description,
        url = this.request.params.proposer.metadata.url,
        icons = this.request.params.proposer.metadata.icons.map { URI(it) },
        chains = this.request.params.permissions.blockchain.chains,
        methods = this.request.params.permissions.jsonRpc.methods,
        types = this.request.params.permissions.notifications.types,
        topic = this.request.params.topic.value,
        proposerPublicKey = this.request.params.proposer.publicKey,
        ttl = this.request.params.ttl.seconds,
        accounts = listOf()
    )

internal fun SessionParamsVO.SessionPayloadParams.toEngineDOSessionRequest(topic: TopicVO, requestId: Long): EngineDO.SessionRequest =
    EngineDO.SessionRequest(
        topic.value,
        chainId,
        EngineDO.SessionRequest.JSONRPCRequest(requestId, this.request.method, this.request.params.toString())
    )

internal fun SessionParamsVO.DeleteParams.toEngineDoDeleteSession(topic: TopicVO): EngineDO.DeletedSession =
    EngineDO.DeletedSession(topic.value, reason.message)

internal fun SessionParamsVO.NotificationParams.toEngineDoSessionNotification(topic: TopicVO): EngineDO.SessionNotification =
    EngineDO.SessionNotification(topic.value, type, data.toString())