package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType
import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.TtlVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.after.params.SessionPermissionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.before.proposal.*
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCRequestVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.pendingSequenceExpirySeconds
import com.walletconnect.walletconnectv2.util.proposedPairingExpirySeconds
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder
import java.util.*

//@JvmSynthetic
//internal fun EngineDO.WalletConnectUri.toPairProposal(): PairingParamsVO.Proposal =
//    PairingParamsVO.Proposal(
//        topic = topic,
//        relay = relay,
//        proposer = PairingProposerVO(publicKey.keyAsHex, isController),
//        signal = PairingSignalVO("uri", PairingSignalParamsVO(toAbsoluteString())),
//        permissions = PairingProposedPermissionsVO(JsonRPCVO(listOf(JsonRpcMethod.WC_SESSION_PROPOSE))),
//        ttl = TtlVO(Duration.days(30).inWholeSeconds)
//    )

@JvmSynthetic
internal fun createPairing(topic: TopicVO, relay: RelayProtocolOptionsVO): PairingVO =
    PairingVO(
        topic,
        ExpiryVO(proposedPairingExpirySeconds()),
        SequenceStatus.PROPOSED,
        PublicKey("null"),
        uri = "uri",
        relay = relay.protocol,
        //todo: data -> relay.data
    )


@JvmSynthetic
internal fun EngineDO.WalletConnectUri.toPairingVO(): PairingVO =
    PairingVO(
        topic,
        ExpiryVO(pendingSequenceExpirySeconds()),
        SequenceStatus.ACKNOWLEDGED,
        PublicKey("null"),
        uri = toAbsoluteString(),
        relay = relay.protocol,
        //todo: data
    )

//todo add optional relay-data
@JvmSynthetic
internal fun EngineDO.WalletConnectUri.toAbsoluteString(): String =
    "wc:${topic.value}@$version?&relay-protocol=${relay.toUrlEncodedString()}symKey=${symKey.keyAsHex}"

//todo add optional relay-data
@JvmSynthetic
internal fun RelayProtocolOptionsVO.toUrlEncodedString(): String =
    URLEncoder.encode(JSONObject().put("protocol", protocol).toString(), "UTF-8")

@JvmSynthetic
internal fun EngineDO.AppMetaData.toMetaDataVO() =
    AppMetaDataVO(name, description, url, icons)

@JvmSynthetic
internal fun AppMetaDataVO.toEngineDOMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toSessionsPermissions(): SessionPermissionsVO =
    SessionPermissionsVO(
        SessionProposedPermissionsVO.Blockchain(blockchain.chains),
        SessionProposedPermissionsVO.JsonRpc(jsonRpc.methods),
        if (notification?.types != null) SessionProposedPermissionsVO.Notifications(notification.types) else null
    )

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toSessionsProposedPermissions(): SessionProposedPermissionsVO =
    SessionProposedPermissionsVO(
        SessionProposedPermissionsVO.Blockchain(blockchain.chains),
        SessionProposedPermissionsVO.JsonRpc(jsonRpc.methods),
        if (notification?.types != null) SessionProposedPermissionsVO.Notifications(notification.types) else null
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
        types = this.request.params.permissions.notifications?.types,
        topic = this.request.params.topic.value,
        publicKey = this.request.params.proposer.publicKey,
        isController = this.request.params.proposer.controller,
        ttl = this.request.params.ttl.seconds,
        accounts = listOf(),
        relayProtocol = request.params.relay.protocol
    )

@JvmSynthetic
internal fun SessionParamsVO.SessionPayloadParams.toEngineDOSessionRequest(request: WCRequestVO): EngineDO.SessionRequest =
    EngineDO.SessionRequest(
        request.topic.value,
        chainId,
        EngineDO.SessionRequest.JSONRPCRequest(request.id, this.request.method, this.request.params.toString())
    )

@JvmSynthetic
internal fun SessionParamsVO.DeleteParams.toEngineDoDeleteSession(topic: TopicVO): EngineDO.SessionDelete =
    EngineDO.SessionDelete(topic.value, reason.message)

@JvmSynthetic
internal fun SessionParamsVO.NotificationParams.toEngineDoSessionNotification(topic: TopicVO): EngineDO.SessionNotification =
    EngineDO.SessionNotification(topic.value, type, data.toString())

@JvmSynthetic
internal fun SessionVO.toEngineDOSettledSessionVO(topic: TopicVO, expiry: ExpiryVO): EngineDO.SettledSession =
    EngineDO.SettledSession(
        topic,
        expiry,
        SequenceStatus.ACKNOWLEDGED,
        accounts,
        EngineDO.AppMetaData(
            metaData?.name ?: String.Empty,
            metaData?.description ?: String.Empty,
            metaData?.url ?: String.Empty,
            metaData?.icons?.map { iconUri -> iconUri } ?: listOf()),
        EngineDO.SettledSession.Permissions(
            EngineDO.SettledSession.Permissions.Blockchain(chains),
            EngineDO.SettledSession.Permissions.JsonRpc(methods),
            EngineDO.SettledSession.Permissions.Notifications(types)
        )
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionProposal(peerPublicKey: PublicKey): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name = metaData?.name ?: String.Empty,
        description = metaData?.description ?: String.Empty,
        url = metaData?.url ?: String.Empty,
        icons = metaData?.icons?.map { URI(it) } ?: emptyList(),
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
        accounts, metaData?.toEngineDOAppMetaData(),
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
    EngineDO.SettledPairing(topic, appMetaDataVO?.toEngineDOAppMetaData())

@JvmSynthetic
internal fun SessionVO.toSessionApproved(params: SessionParamsVO.ApprovalParams, settledTopic: TopicVO): EngineDO.SessionApproved =
    EngineDO.SessionApproved(
        settledTopic.value,
        params.responder.metadata?.toEngineDOAppMetaData(),
        EngineDO.SessionPermissions(EngineDO.Blockchain(chains), EngineDO.JsonRpc(methods)),
        params.state.accounts
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
        types = permissions.notifications?.types,
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
internal fun EngineDO.SessionProposal.toRespondedSessionVO(
    selfPublicKey: PublicKey,
    settledTopic: TopicVO,
    controllerType: ControllerType
): SessionVO =
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
        relayProtocol = relayProtocol,
        outcomeTopic = settledTopic
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
        types ?: emptyList(),
        TtlVO(ttl),
        accounts,
        controllerType = controllerType,
        relayProtocol = relayProtocol
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOAcknowledgeSessionVO(settledTopic: TopicVO, params: SessionParamsVO.ApprovalParams): SessionVO =
    SessionVO(
        settledTopic,
        params.expiry,
        SequenceStatus.ACKNOWLEDGED,
        selfParticipant,
        PublicKey(params.responder.publicKey),
        controllerKey = if (controllerType == ControllerType.CONTROLLER) selfParticipant else PublicKey(params.responder.publicKey),
        controllerType = controllerType,
        metaData = params.responder.metadata,
        relayProtocol = params.relay.protocol,
        chains = chains,
        methods = methods,
        types = types,
        accounts = params.state.accounts,
        ttl = TtlVO(params.expiry.seconds)
    )

//@JvmSynthetic
//internal fun PairingVO.toAcknowledgedPairingVO(
//    settledTopic: TopicVO,
//    params: PairingParamsVO.ApproveParams,
//    controllerType: ControllerType
//): PairingVO =
//    PairingVO(
//        settledTopic,
//        params.expiry,
//        SequenceStatus.ACKNOWLEDGED,
//        selfParticipant,
//        PublicKey(params.responder.publicKey),
//        controllerKey = if (controllerType == ControllerType.CONTROLLER) selfParticipant else PublicKey(params.responder.publicKey),
//        uri,
//        permissions = permissions,
//        relay = relay,
//        controllerType = controllerType,
//        appMetaDataVO = params.state?.metadata
//    )

@JvmSynthetic
internal fun SessionParamsVO.ProposalParams.toEngineDOPendingSessionVO(
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
        types = permissions.notifications?.types,
        ttl = TtlVO(pendingSequenceExpirySeconds()),
        controllerType = controllerType,
        relayProtocol = relay.protocol
    )

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcResult.toEngineJsonRpcResult(): EngineDO.JsonRpcResponse.JsonRpcResult =
    EngineDO.JsonRpcResponse.JsonRpcResult(id = id, result = result)

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcError.toEngineJsonRpcError(): EngineDO.JsonRpcResponse.JsonRpcError =
    EngineDO.JsonRpcResponse.JsonRpcError(id = id, error = EngineDO.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun EngineDO.SessionProposal.toSessionPermissions(): EngineDO.SessionPermissions =
    EngineDO.SessionPermissions(
        blockchain = EngineDO.Blockchain(chains),
        jsonRpc = EngineDO.JsonRpc(methods),
        notification = if (types != null) EngineDO.Notifications(types) else null
    )

@JvmSynthetic
internal fun SessionPermissionsVO.toEngineDOPermissions(): EngineDO.SessionPermissions =
    EngineDO.SessionPermissions(
        blockchain = EngineDO.Blockchain(blockchain.chains),
        jsonRpc = EngineDO.JsonRpc(jsonRpc.methods),
        notification = if (notifications?.types != null) EngineDO.Notifications(notifications.types) else null
    )

