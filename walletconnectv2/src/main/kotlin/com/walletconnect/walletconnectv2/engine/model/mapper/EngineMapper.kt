package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.common.model.type.ControllerType
import com.walletconnect.walletconnectv2.common.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.common.model.vo.*
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.PairingParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.PreSettlementPairingVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.proposal.*
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingParticipantVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingStateVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params.SessionPermissionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.*
import com.walletconnect.walletconnectv2.common.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.pendingSequenceExpirySeconds
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder
import java.util.*
import kotlin.time.Duration

@JvmSynthetic
internal fun String.toPairProposal(): PairingParamsVO.Proposal {
    val properUriString = if (contains("wc://")) this else replace("wc:", "wc://")
    val pairUri = URI(properUriString)
    val mapOfQueryParameters: Map<String, String> =
        pairUri.query.split("&").associate { query -> query.substringBefore("=") to query.substringAfter("=") }
    val relay = JSONObject(mapOfQueryParameters["relay"] ?: "{}").getString("protocol") ?: String.Empty
    val publicKey = mapOfQueryParameters["publicKey"] ?: ""
    val controller: Boolean = mapOfQueryParameters["controller"].toBoolean()
    val ttl: Long = Duration.days(30).inWholeSeconds
    return PairingParamsVO.Proposal(
        topic = TopicVO(pairUri.userInfo),
        relay = RelayProtocolOptionsVO(relay),
        proposer = PairingProposerVO(publicKey, controller),
        signal = PairingSignalVO("uri", PairingSignalParamsVO(properUriString)),
        permissions = PairingProposedPermissionsVO(JsonRPCVO(listOf(JsonRpcMethod.WC_SESSION_PROPOSE))),
        ttl = TtlVO(ttl)
    )
}

@JvmSynthetic
internal fun EngineDO.WalletConnectUri.toAbsoluteString(): String =
    "wc:${topic.value}@$version?controller=$isController&publicKey=${publicKey.keyAsHex}&relay=${relay.toUrlEncodedString()}"

@JvmSynthetic
internal fun RelayProtocolOptionsVO.toUrlEncodedString(): String = URLEncoder.encode(JSONObject().put("protocol", protocol).toString(), "UTF-8")

@JvmSynthetic
internal fun PairingParamsVO.Proposal.toPairingSuccess(expiry: ExpiryVO, selfPublicKey: PublicKey): PairingParamsVO.ApproveParams =
    PairingParamsVO.ApproveParams(
        relay = relay,
        responder = PairingParticipantVO(publicKey = selfPublicKey.keyAsHex),
        expiry = expiry,
        state = PairingStateVO(null)
    )

@JvmSynthetic
internal fun PairingParamsVO.Proposal.toApprove(id: Long, expiry: ExpiryVO, selfPublicKey: PublicKey): PreSettlementPairingVO.Approve =
    PreSettlementPairingVO.Approve(id = id, params = this.toPairingSuccess(expiry, selfPublicKey))

@JvmSynthetic
internal fun EngineDO.AppMetaData.toMetaDataVO() =
    AppMetaDataVO(name, description, url, icons)

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toSessionsPermissions(): SessionPermissionsVO =
    SessionPermissionsVO(SessionProposedPermissionsVO.Blockchain(blockchain.chains), SessionProposedPermissionsVO.JsonRpc(jsonRpc.methods))

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toSessionsProposedPermissions(): SessionProposedPermissionsVO =
    SessionProposedPermissionsVO(
        SessionProposedPermissionsVO.Blockchain(blockchain.chains),
        SessionProposedPermissionsVO.JsonRpc(jsonRpc.methods)
    )

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.JsonRpcResult.toJsonRpcResult(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result = result)

@JvmSynthetic
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
        publicKey = this.request.params.proposer.publicKey,
        isController = this.request.params.proposer.controller,
        ttl = this.request.params.ttl.seconds,
        accounts = listOf(),
        relayProtocol = request.params.relay.protocol
    )

@JvmSynthetic
internal fun SessionParamsVO.SessionPayloadParams.toEngineDOSessionRequest(topic: TopicVO, requestId: Long): EngineDO.SessionRequest =
    EngineDO.SessionRequest(
        topic.value,
        chainId,
        EngineDO.SessionRequest.JSONRPCRequest(requestId, this.request.method, this.request.params.toString())
    )

@JvmSynthetic
internal fun SessionParamsVO.DeleteParams.toEngineDoDeleteSession(topic: TopicVO): EngineDO.DeletedSession =
    EngineDO.DeletedSession(topic.value, reason.message)

@JvmSynthetic
internal fun SessionParamsVO.NotificationParams.toEngineDoSessionNotification(topic: TopicVO): EngineDO.SessionNotification =
    EngineDO.SessionNotification(topic.value, type, data.toString())

@JvmSynthetic
internal fun EngineDO.SessionProposal.toEngineDOSettledSessionVO(topic: TopicVO, expiry: ExpiryVO): EngineDO.SettledSession =
    EngineDO.SettledSession(
        topic,
        expiry,
        SequenceStatus.ACKNOWLEDGED,
        accounts,
        EngineDO.AppMetaData(name, description, url, icons.map { iconUri -> iconUri.toString() }),
        EngineDO.SettledSession.Permissions(
            EngineDO.SettledSession.Permissions.Blockchain(chains),
            EngineDO.SettledSession.Permissions.JsonRpc(methods),
            EngineDO.SettledSession.Permissions.Notifications(types)
        )
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionProposal(peerPublicKey: PublicKey): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name = appMetaData?.name ?: String.Empty,
        description = appMetaData?.description ?: String.Empty,
        url = appMetaData?.url ?: String.Empty,
        icons = appMetaData?.icons?.map { URI(it) } ?: emptyList(),
        chains = chains,
        methods = methods,
        types = types,
        topic = topic.value,
        publicKey = peerPublicKey.keyAsHex,
        ttl = ttl.seconds,
        accounts = accounts,
        relayProtocol = relayProtocol
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSettledSessionVO(): EngineDO.SettledSession =
    EngineDO.SettledSession(
        topic, expiry, status,
        accounts, appMetaData?.toEngineDOAppMetaData(),
        EngineDO.SettledSession.Permissions(
            EngineDO.SettledSession.Permissions.Blockchain(chains),
            EngineDO.SettledSession.Permissions.JsonRpc(methods),
            EngineDO.SettledSession.Permissions.Notifications(types)
        )
    )

@JvmSynthetic
private fun AppMetaDataVO.toEngineDOAppMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun PairingVO.toEngineDOSettledPairing(): EngineDO.SettledPairing =
    EngineDO.SettledPairing(topic, relay, appMetaDataVO?.toEngineDOAppMetaData())

@JvmSynthetic
internal fun SessionVO.toSessionApproved(metaDataVO: AppMetaDataVO?, settledTopic: TopicVO): EngineDO.SessionApproved =
    EngineDO.SessionApproved(
        settledTopic.value,
        metaDataVO?.toEngineDOAppMetaData(),
        EngineDO.SessionPermissions(EngineDO.Blockchain(chains), EngineDO.JsonRpc(methods))
    )

@JvmSynthetic
internal fun SessionParamsVO.ProposalParams.toProposedSessionVO(
    topic: TopicVO,
    selfPublicKey: PublicKey,
    controllerType: ControllerType
): SessionVO =
    SessionVO(
        topic,
        ExpiryVO(pendingSequenceExpirySeconds()),
        SequenceStatus.PROPOSED,
        selfPublicKey,
        chains = permissions.blockchain.chains,
        methods = permissions.jsonRpc.methods,
        types = permissions.notifications.types,
        ttl = TtlVO(pendingSequenceExpirySeconds()),
        controllerType = controllerType,
        relayProtocol = relay.protocol
    )

@JvmSynthetic
internal fun SessionProposerVO.toProposalParams(
    pendingTopic: TopicVO,
    settleTopic: TopicVO,
    permissions: EngineDO.SessionPermissions
): SessionParamsVO.ProposalParams =
    SessionParamsVO.ProposalParams(
        pendingTopic,
        RelayProtocolOptionsVO(),
        this,
        SessionSignalVO(params = SessionSignalVO.Params(settleTopic)),
        permissions.toSessionsProposedPermissions(),
        TtlVO(pendingSequenceExpirySeconds())
    )

@JvmSynthetic
internal fun PairingParamsVO.Proposal.toRespondedPairingVO(
    topic: TopicVO,
    selfPublicKey: PublicKey,
    uri: String,
    controllerType: ControllerType
): PairingVO =
    PairingVO(
        topic,
        ExpiryVO(pendingSequenceExpirySeconds()),
        SequenceStatus.RESPONDED,
        selfPublicKey,
        uri = uri,
        relay = relay.protocol,
        controllerType = controllerType
    )

@JvmSynthetic
internal fun PairingParamsVO.Proposal.toPreSettledPairingVO(
    topic: TopicVO,
    selfPublicKey: PublicKey,
    uri: String,
    controllerType: ControllerType
): PairingVO =
    PairingVO(
        topic,
        ExpiryVO((Calendar.getInstance().timeInMillis / 1000) + ttl.seconds),
        SequenceStatus.PRE_SETTLED,
        selfPublicKey,
        PublicKey(proposer.publicKey),
        relay = relay.protocol,
        controllerKey = if (proposer.controller) PublicKey(proposer.publicKey) else selfPublicKey,
        uri = uri,
        permissions = permissions?.jsonRPC?.methods,
        controllerType = controllerType
    )

@JvmSynthetic
internal fun EngineDO.SessionProposal.toRespondedSessionVO(selfPublicKey: PublicKey, controllerType: ControllerType): SessionVO =
    SessionVO(
        TopicVO(topic),
        ExpiryVO(pendingSequenceExpirySeconds()),
        SequenceStatus.RESPONDED,
        selfPublicKey,
        chains = chains,
        methods = methods,
        types = types,
        ttl = TtlVO(ttl),
        controllerType = controllerType,
        relayProtocol = relayProtocol
    )

@JvmSynthetic
internal fun EngineDO.SessionProposal.toPreSettledSessionVO(
    settledTopic: TopicVO,
    selfPublicKey: PublicKey,
    controllerType: ControllerType
): SessionVO =
    SessionVO(
        settledTopic,
        ExpiryVO((Calendar.getInstance().timeInMillis / 1000) + ttl),
        SequenceStatus.PRE_SETTLED,
        selfPublicKey,
        PublicKey(publicKey),
        if (isController) PublicKey(publicKey) else selfPublicKey,
        chains,
        methods,
        types,
        TtlVO(ttl),
        accounts,
        controllerType = controllerType,
        relayProtocol = relayProtocol
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSettledSessionVO(settledTopic: TopicVO, params: SessionParamsVO.ApprovalParams): SessionVO =
    SessionVO(
        settledTopic,
        params.expiry,
        SequenceStatus.ACKNOWLEDGED,
        selfParticipant,
        PublicKey(params.responder.publicKey),
        controllerKey = if (controllerType == ControllerType.CONTROLLER) selfParticipant else PublicKey(params.responder.publicKey),
        controllerType = controllerType,
        appMetaData = params.responder.metadata,
        relayProtocol = params.relay.protocol,
        chains = chains,
        methods = methods,
        types = types,
        accounts = params.state.accounts,
        ttl = TtlVO(params.expiry.seconds)
    )

@JvmSynthetic
internal fun PairingVO.toAcknowledgedPairingVO(
    settledTopic: TopicVO,
    params: PairingParamsVO.ApproveParams,
    controllerType: ControllerType
): PairingVO =
    PairingVO(
        settledTopic,
        params.expiry,
        SequenceStatus.ACKNOWLEDGED,
        selfParticipant,
        PublicKey(params.responder.publicKey),
        controllerKey = if (controllerType == ControllerType.CONTROLLER) selfParticipant else PublicKey(params.responder.publicKey),
        uri,
        permissions = permissions,
        relay = relay,
        controllerType = controllerType,
        appMetaDataVO = params.state.metadata
    )

@JvmSynthetic
internal fun SessionParamsVO.ProposalParams.toEngineDOSettledSessionVO(selfPublicKey: PublicKey, controllerType: ControllerType): SessionVO =
    SessionVO(
        topic,
        ExpiryVO(pendingSequenceExpirySeconds()),
        SequenceStatus.PROPOSED,
        selfPublicKey,
        chains = permissions.blockchain.chains,
        methods = permissions.jsonRpc.methods,
        types = permissions.notifications.types,
        ttl = TtlVO(pendingSequenceExpirySeconds()),
        controllerType = controllerType,
        relayProtocol = relay.protocol
    )

@JvmSynthetic
internal fun EngineDO.WalletConnectUri.toProposedPairingVO(controllerType: ControllerType): PairingVO =
    PairingVO(
        topic,
        ExpiryVO(pendingSequenceExpirySeconds()),
        SequenceStatus.PROPOSED,
        publicKey,
        uri = toAbsoluteString(),
        relay = relay.protocol,
        controllerType = controllerType
    )

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcResult.toEngineJsonRpcResult(): EngineDO.JsonRpcResponse.JsonRpcResult =
    EngineDO.JsonRpcResponse.JsonRpcResult(id = id, result = result)