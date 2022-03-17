package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.*
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.BlockchainProposedVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.SessionProposerVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.BlockchainSettledVO
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
    MetaDataVO(name, description, url, icons)

@JvmSynthetic
internal fun MetaDataVO.toEngineDOMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun PairingParamsVO.SessionProposeParams.toEngineDOSessionProposal(): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name = this.proposer.metadata?.name ?: String.Empty,
        description = this.proposer.metadata?.description ?: String.Empty,
        url = this.proposer.metadata?.url ?: String.Empty,
        icons = this.proposer.metadata?.icons?.map { URI(it) } ?: listOf(),
        chains = this.blockchainProposedVO.chains,
        methods = this.permissions.jsonRpc.methods,
        types = this.permissions.notifications?.types,
        proposerPublicKey = this.proposer.publicKey,
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
internal fun SessionVO.toEngineDOApprovedSessionVO(topic: TopicVO): EngineDO.Session =
    EngineDO.Session(
        topic,
        expiry,
        accounts,
        EngineDO.AppMetaData(
            selfMetaData?.name ?: String.Empty,
            selfMetaData?.description ?: String.Empty,
            selfMetaData?.url ?: String.Empty,
            selfMetaData?.icons?.map { iconUri -> iconUri } ?: listOf()),
        EngineDO.SessionPermissions(EngineDO.SessionPermissions.JsonRpc(methods), getNotifications(types)),
        EngineDO.Blockchain(chains),
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOApprovedSessionVO(): EngineDO.Session =
    EngineDO.Session(
        topic, expiry,
        accounts, selfMetaData?.toEngineDOAppMetaData(),
        EngineDO.SessionPermissions(EngineDO.SessionPermissions.JsonRpc(methods), getNotifications(types)),
        EngineDO.Blockchain(chains),
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOExtendedSessionVO(expiryVO: ExpiryVO): EngineDO.SessionExtend =
    EngineDO.SessionExtend(
        topic, expiryVO,
        accounts, selfMetaData?.toEngineDOAppMetaData(),
        EngineDO.SessionPermissions(EngineDO.SessionPermissions.JsonRpc(methods), getNotifications(types)),
        EngineDO.Blockchain(chains),
    )

@JvmSynthetic
private fun MetaDataVO.toEngineDOAppMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun PairingVO.toEngineDOSettledPairing(): EngineDO.PairingSettle =
    EngineDO.PairingSettle(topic, peerMetaData?.toEngineDOAppMetaData())

@JvmSynthetic
internal fun SessionVO.toSessionApproved(): EngineDO.SessionApproved =
    EngineDO.SessionApproved(
        topic.value,
        peerMetaData?.toEngineDOMetaData(),
        EngineDO.SessionPermissions(EngineDO.SessionPermissions.JsonRpc(methods),
            if (types != null) EngineDO.SessionPermissions.Notifications(types) else null),
        accounts
    )

@JvmSynthetic
internal fun EngineDO.SessionProposal.toSessionSettleParams(
    selfParticipant: SessionParticipantVO,
    sessionExpiry: Long,
): SessionParamsVO.SessionSettleParams =
    SessionParamsVO.SessionSettleParams(
        relay = RelayProtocolOptionsVO(relayProtocol, relayData),
        blockchain = BlockchainSettledVO(accounts = accounts, chains),
        permission = SessionPermissionsVO(JsonRpcVO(methods = methods),
            notifications = if (types != null) NotificationsVO(types) else null),
        controller = selfParticipant,
        expiryTimestamp = sessionExpiry)

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toSessionsPermissionsVO(): SessionPermissionsVO =
    SessionPermissionsVO(
        JsonRpcVO(jsonRpc.methods),
        if (notifications?.types != null) NotificationsVO(notifications.types) else null
    )

@JvmSynthetic
internal fun EngineDO.Blockchain.toSessionProposeParams(
    relay: RelayProtocolOptionsVO,
    permissions: EngineDO.SessionPermissions,
    selfPublicKey: PublicKey,
    metaData: EngineDO.AppMetaData,
) = PairingParamsVO.SessionProposeParams(
    relays = listOf(relay),
    blockchainProposedVO = BlockchainProposedVO(chains),
    permissions = permissions.toSessionsPermissionsVO(),
    proposer = SessionProposerVO(selfPublicKey.keyAsHex, metaData.toMetaDataVO())
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
        jsonRpc = EngineDO.SessionPermissions.JsonRpc(methods),
        notifications = getNotifications(types)
    )

@JvmSynthetic
internal fun EngineDO.SessionProposal.toSessionApproveParams(selfPublicKey: PublicKey): SessionParamsVO.ApprovalParams =
    SessionParamsVO.ApprovalParams(
        relay = RelayProtocolOptionsVO(relayProtocol, relayData),
        responder = AgreementPeer(selfPublicKey.keyAsHex))

@JvmSynthetic
internal fun SessionPermissionsVO.toEngineDOPermissions(): EngineDO.SessionPermissions =
    EngineDO.SessionPermissions(
        jsonRpc = EngineDO.SessionPermissions.JsonRpc(jsonRpc.methods),
        notifications = getNotifications(notifications?.types)
    )

@JvmSynthetic
private fun getNotifications(types: List<String>?) = if (types != null) EngineDO.SessionPermissions.Notifications(types) else null

