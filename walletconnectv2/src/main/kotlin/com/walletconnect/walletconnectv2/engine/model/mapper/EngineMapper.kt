package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.AppMetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionPermissionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.JsonRpcVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.NotificationsVO
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.core.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.WCRequestVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.util.Empty
import java.net.URI

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
        publicKey = peerPublicKey.keyAsHex,
        accounts = accounts,
        relayProtocol = relayProtocol,
        relayData = relayData
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSettledSessionVO(): EngineDO.Session =
    EngineDO.Session(
        topic, expiry,
        accounts, selfMetaData?.toEngineDOAppMetaData(),
        EngineDO.SessionPermissions(EngineDO.JsonRpc(methods), getNotifications(types)),
        EngineDO.Blockchain(chains),
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOExtendedSessionVO(expiryVO: ExpiryVO): EngineDO.SessionExtend =
    EngineDO.SessionExtend(
        topic, expiryVO,
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

@JvmSynthetic
internal fun SessionVO.toSessionApproved(): EngineDO.SessionApproved =
    EngineDO.SessionApproved(
        topic.value,
        peerMetaData?.toEngineDOMetaData(),
        EngineDO.SessionPermissions(EngineDO.JsonRpc(methods), if (types != null) EngineDO.Notifications(types) else null),
        accounts
    )

//@JvmSynthetic
//internal fun SessionProposerVO.toProposalParams(
//    pendingTopic: TopicVO,
//    settleTopic: TopicVO,
//    permissions: EngineDO.SessionPermissions,
//    blockchain: EngineDO.Blockchain,
//): PairingParamsVO.SessionProposeParams =
//    PairingParamsVO.SessionProposeParams(
//        relays = listOf(RelayProtocolOptionsVO()),
//        blockchainProposedVO = BlockchainProposedVO(blockchain.chains),
//        permissions = permissions.toSessionsPermissions(),
//        proposer = this
////        topic = pendingTopic //todo: pending or settled topic?
//    )

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toSessionsPermissions(): SessionPermissionsVO =
    SessionPermissionsVO(
        JsonRpcVO(jsonRpc.methods),
        if (notifications?.types != null) NotificationsVO(notifications.types) else null
    )

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcResult.toEngineJsonRpcResult(): EngineDO.JsonRpcResponse.JsonRpcResult =
    EngineDO.JsonRpcResponse.JsonRpcResult(id = id, result = result.toString())

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

