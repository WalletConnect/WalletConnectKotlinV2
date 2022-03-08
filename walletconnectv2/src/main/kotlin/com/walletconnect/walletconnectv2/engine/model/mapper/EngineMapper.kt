package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AppMetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionPermissionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.BlockchainProposedVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.SessionProposerVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.JsonRpcVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.NotificationsVO
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCRequestVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty
import com.walletconnect.walletconnectv2.util.Time
import com.walletconnect.walletconnectv2.util.pendingSequenceExpirySeconds
import com.walletconnect.walletconnectv2.util.proposedPairingExpirySeconds
import java.net.URI
import java.util.*

@JvmSynthetic
internal fun createPairing(topic: TopicVO, relay: RelayProtocolOptionsVO, uri: String): PairingVO =
    PairingVO(
        topic,
        ExpiryVO(proposedPairingExpirySeconds()), //todo: change to 5mins?
        SequenceStatus.ACKNOWLEDGED, //todo: remove states from pairings?
        uri = uri,
        relayProtocol = relay.protocol,
        relayData = relay.data
    )

@JvmSynthetic
internal fun EngineDO.WalletConnectUri.toPairingVO(): PairingVO =
    PairingVO(
        topic,
        ExpiryVO(pendingSequenceExpirySeconds()), //todo: change to 5mins?
        SequenceStatus.ACKNOWLEDGED, //todo: remove states from pairings?
        uri = toAbsoluteString(),
        relayProtocol = relay.protocol,
        relayData = relay.data
    )

@JvmSynthetic
internal fun PairingParamsVO.SessionProposeParams.toProposedSessionVO(topic: TopicVO): SessionVO =
    SessionVO(
        topic,
        ExpiryVO(pendingSequenceExpirySeconds()),
        SequenceStatus.PROPOSED,
        relayProtocol = relays.first().protocol,
        relayData = relays.first().data,
        selfParticipant = PublicKey(proposer.publicKey),
        selfMetaData = proposer.metadata,
        chains = blockchainProposedVO.chains,
        methods = permissions.jsonRpc.methods,
        types = permissions.notifications?.types,
    )

@JvmSynthetic
internal fun EngineDO.WalletConnectUri.toAbsoluteString(): String =
    "wc:${topic.value}@$version?${getQuery()}&symKey=${symKey.keyAsHex}"

private fun EngineDO.WalletConnectUri.getQuery(): String {
    var query = "relay-protocol=${relay.protocol}"
    if (relay.data != null) {
        query = "$query&relay-data=${relay.data}"
    }
    return query
}

@JvmSynthetic
internal fun EngineDO.AppMetaData.toMetaDataVO() =
    AppMetaDataVO(name, description, url, icons)

@JvmSynthetic
internal fun AppMetaDataVO.toEngineDOMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.JsonRpcResult.toJsonRpcResult(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun PairingParamsVO.SessionProposeParams.toEngineDOSessionProposal(): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name = this.proposer.metadata?.name!!,
        description = this.proposer.metadata.description,
        url = this.proposer.metadata.url,
        icons = this.proposer.metadata.icons.map { URI(it) },
        chains = this.blockchainProposedVO.chains,
        methods = this.permissions.jsonRpc.methods,
        types = this.permissions.notifications?.types,
        topic = "", //todo: assign correct topic
        publicKey = this.proposer.publicKey,
        accounts = listOf(),
        relayProtocol = relays.first().protocol,
        relayData = relays.first().data
    )

@JvmSynthetic
internal fun SessionParamsVO.SessionRequestParams.toEngineDOSessionRequest(request: WCRequestVO): EngineDO.SessionRequest =
    EngineDO.SessionRequest(
        request.topic.value,
        chainId,
        EngineDO.SessionRequest.JSONRPCRequest(request.id, this.request.method, this.request.params.toString())
    )

@JvmSynthetic
internal fun SessionParamsVO.DeleteParams.toEngineDoDeleteSession(topic: TopicVO): EngineDO.SessionDelete =
    EngineDO.SessionDelete(topic.value, reason.message)

@JvmSynthetic
internal fun SessionParamsVO.NotifyParams.toEngineDoSessionNotification(topic: TopicVO): EngineDO.SessionNotification =
    EngineDO.SessionNotification(topic.value, type, data.toString())

@JvmSynthetic
internal fun SessionVO.toEngineDOSettledSessionVO(topic: TopicVO, expiry: ExpiryVO): EngineDO.Session =
    EngineDO.Session(
        topic,
        expiry,
        SequenceStatus.ACKNOWLEDGED,
        accounts,
        EngineDO.AppMetaData(
            selfMetaData?.name ?: String.Empty,
            selfMetaData?.description ?: String.Empty,
            selfMetaData?.url ?: String.Empty,
            selfMetaData?.icons?.map { iconUri -> iconUri } ?: listOf()),
        EngineDO.SessionPermissions(EngineDO.JsonRpc(methods), getNotifications(types)),
        EngineDO.Blockchain(chains),
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionProposal(peerPublicKey: PublicKey): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name = selfMetaData?.name ?: String.Empty,
        description = selfMetaData?.description ?: String.Empty,
        url = selfMetaData?.url ?: String.Empty,
        icons = selfMetaData?.icons?.map { URI(it) } ?: emptyList(),
        chains = chains,
        methods = methods,
        types = types,
        topic = topic.value,
        publicKey = peerPublicKey.keyAsHex,
        accounts = accounts,
        relayProtocol = relayProtocol,
        relayData = relayData
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSettledSessionVO(): EngineDO.Session =
    EngineDO.Session(
        topic, expiry, status,
        accounts, selfMetaData?.toEngineDOAppMetaData(),
        EngineDO.SessionPermissions(EngineDO.JsonRpc(methods), getNotifications(types)),
        EngineDO.Blockchain(chains),
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOExtendedSessionVO(expiryVO: ExpiryVO): EngineDO.SessionExtend =
    EngineDO.SessionExtend(
        topic, expiryVO, status,
        accounts, selfMetaData?.toEngineDOAppMetaData(),
        EngineDO.SessionPermissions(EngineDO.JsonRpc(methods), getNotifications(types)),
        EngineDO.Blockchain(chains),
    )

@JvmSynthetic
private fun AppMetaDataVO.toEngineDOAppMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun PairingVO.toEngineDOSettledPairing(): EngineDO.PairingSettle =
    EngineDO.PairingSettle(topic, selfMetaData?.toEngineDOAppMetaData())

//@JvmSynthetic
//internal fun SessionVO.toSessionApproved(params: SessionParamsVO.ApprovalParams, settledTopic: TopicVO): EngineDO.SessionApproved =
//    EngineDO.SessionApproved(
//        settledTopic.value,
//        params.responder.metadata?.toEngineDOAppMetaData(),
//        EngineDO.SessionPermissions(EngineDO.Blockchain(chains), EngineDO.JsonRpc(methods)),
//        params.state.accounts
//    )


@JvmSynthetic
internal fun SessionProposerVO.toProposalParams(
    pendingTopic: TopicVO,
    settleTopic: TopicVO,
    permissions: EngineDO.SessionPermissions,
    blockchain: EngineDO.Blockchain
): PairingParamsVO.SessionProposeParams =
    PairingParamsVO.SessionProposeParams(
        relays = listOf(RelayProtocolOptionsVO()),
        blockchainProposedVO = BlockchainProposedVO(blockchain.chains),
        permissions = permissions.toSessionsPermissions(),
        proposer = this
//        topic = pendingTopic //todo: pending or settled topic?
    )

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toSessionsPermissions(): SessionPermissionsVO =
    SessionPermissionsVO(
        JsonRpcVO(jsonRpc.methods),
        if (notifications?.types != null) NotificationsVO(notifications.types) else null
    )

@JvmSynthetic
internal fun EngineDO.SessionProposal.toRespondedSessionVO(selfPublicKey: PublicKey, settledTopic: TopicVO): SessionVO =
    SessionVO(
        TopicVO(topic),
        ExpiryVO(pendingSequenceExpirySeconds()),
        SequenceStatus.RESPONDED,
        relayProtocol = relayProtocol,
        relayData = relayData,
        selfParticipant = selfPublicKey,
        chains = chains,
        methods = methods,
        types = types,
        outcomeTopic = settledTopic
    )

@JvmSynthetic
internal fun EngineDO.SessionProposal.toPreSettledSessionVO(settledTopic: TopicVO, selfPublicKey: PublicKey): SessionVO =
    SessionVO(
        settledTopic,
        ExpiryVO((Calendar.getInstance().timeInMillis / 1000) + Time.dayInSeconds), //todo: add proper expiry
        SequenceStatus.PRE_SETTLED,
        relayProtocol = relayProtocol,
        relayData = relayData,
        selfParticipant = selfPublicKey,
        peerParticipant = PublicKey(publicKey),
        controllerKey = PublicKey(publicKey),//todo: which is controller key else selfPublicKey,
        chains = chains,
        methods = methods,
        types = types ?: emptyList(),
        accounts = accounts
    )

//@JvmSynthetic
//internal fun SessionVO.toEngineDOAcknowledgeSessionVO(settledTopic: TopicVO, params: SessionParamsVO.ApprovalParams): SessionVO =
//    SessionVO(
//        settledTopic,
//        params.expiry,
//        SequenceStatus.ACKNOWLEDGED,
//        selfParticipant,
//        PublicKey(params.responder.publicKey),
//        controllerKey = PublicKey(params.responder.publicKey), //todo: should always set responder key as controller ?
//        metaData = params.responder.metadata,
//        relayProtocol = params.relay.protocol,
//        chains = chains,
//        methods = methods,
//        types = types,
//        accounts = params.state.accounts,
//        ttl = TtlVO(params.expiry.seconds)
//    )

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

//@JvmSynthetic
//internal fun SessionParamsVO.ProposalParams.toEngineDOPendingSessionVO(selfPublicKey: PublicKey): SessionVO =
//    SessionVO(
//        topic,
//        ExpiryVO(pendingSequenceExpirySeconds()),
//        SequenceStatus.PROPOSED,
//        selfPublicKey,
//        chains = permissions.blockchain.chains,
//        methods = permissions.jsonRpc.methods,
//        types = permissions.notifications?.types,
//        ttl = TtlVO(pendingSequenceExpirySeconds()),
//        relayProtocol = relay.protocol
//    )

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcResult.toEngineJsonRpcResult(): EngineDO.JsonRpcResponse.JsonRpcResult =
    EngineDO.JsonRpcResponse.JsonRpcResult(id = id, result = result)

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcError.toEngineJsonRpcError(): EngineDO.JsonRpcResponse.JsonRpcError =
    EngineDO.JsonRpcResponse.JsonRpcError(id = id, error = EngineDO.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun EngineDO.SessionProposal.toSessionPermissions(): EngineDO.SessionPermissions =
    EngineDO.SessionPermissions(
        jsonRpc = EngineDO.JsonRpc(methods),
        notifications = getNotifications(types)
    )

@JvmSynthetic
internal fun SessionPermissionsVO.toEngineDOPermissions(): EngineDO.SessionPermissions =
    EngineDO.SessionPermissions(
        jsonRpc = EngineDO.JsonRpc(jsonRpc.methods),
        notifications = getNotifications(notifications?.types)
    )

@JvmSynthetic
private fun getNotifications(types: List<String>?) = if (types != null) EngineDO.Notifications(types) else null

