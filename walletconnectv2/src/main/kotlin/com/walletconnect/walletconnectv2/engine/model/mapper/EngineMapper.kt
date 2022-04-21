package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.payload.SessionProposerVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.params.SessionParamsVO
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
        name = this.proposer.metadata.name,
        description = this.proposer.metadata.description,
        url = this.proposer.metadata.url,
        icons = this.proposer.metadata.icons.map { URI(it) },
        chains = this.chains,
        methods = this.methods,
        events = this.events,
        proposerPublicKey = this.proposer.publicKey,
        accounts = listOf(),
        relayProtocol = relays.first().protocol,
        relayData = relays.first().data
    )

@JvmSynthetic
internal fun SessionParamsVO.SessionRequestParams.toEngineDOSessionRequest(request: WCRequestVO, peerMetaDataVO: MetaDataVO?): EngineDO.SessionRequest =
    EngineDO.SessionRequest(
        topic = request.topic.value,
        chainId = chainId,
        peerAppMetaData = peerMetaDataVO?.toEngineDOAppMetaData(),
        request = EngineDO.SessionRequest.JSONRPCRequest(
            id = request.id,
            method = this.request.method,
            params = this.request.params.toString()
        )
    )

@JvmSynthetic
internal fun SessionParamsVO.DeleteParams.toEngineDoDeleteSession(topic: TopicVO): EngineDO.SessionDelete =
    EngineDO.SessionDelete(topic.value, message)

@JvmSynthetic
internal fun SessionParamsVO.EventParams.toEngineDOSessionEvent(topic: TopicVO): EngineDO.SessionEvent =
    EngineDO.SessionEvent(topic.value, event.name, event.data.toString(), chainId)

@JvmSynthetic
internal fun SessionVO.toEngineDOApprovedSessionVO(topic: TopicVO): EngineDO.Session =
    EngineDO.Session(
        topic, expiry, accounts, methods, events,
        EngineDO.AppMetaData(
            selfMetaData?.name ?: String.Empty,
            selfMetaData?.description ?: String.Empty,
            selfMetaData?.url ?: String.Empty,
            selfMetaData?.icons?.map { iconUri -> iconUri } ?: listOf())
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOApprovedSessionVO(): EngineDO.Session =
    EngineDO.Session(
        topic, expiry, accounts, methods, events,
        selfMetaData?.toEngineDOAppMetaData()
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionUpdateExpiry(expiryVO: ExpiryVO): EngineDO.SessionUpdateExpiry =
    EngineDO.SessionUpdateExpiry(
        topic, expiryVO, accounts, methods, events,
        selfMetaData?.toEngineDOAppMetaData()
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
        accounts, methods, events,
    )

@JvmSynthetic
internal fun PairingParamsVO.SessionProposeParams.toSessionSettleParams(
    selfParticipant: SessionParticipantVO,
    sessionExpiry: Long,
    accounts: List<String>,
    methods: List<String>,
    events: List<String>,
): SessionParamsVO.SessionSettleParams =
    SessionParamsVO.SessionSettleParams(
        relay = RelayProtocolOptionsVO(relays.first().protocol, relays.first().data),
        controller = selfParticipant,
        accounts = accounts,
        methods = methods,
        events = events,
        expiry = sessionExpiry)

@JvmSynthetic
internal fun toSessionProposeParams(
    relay: RelayProtocolOptionsVO,
    chains: List<String>, methods: List<String>, events: List<String>,
    selfPublicKey: PublicKey,
    metaData: EngineDO.AppMetaData,
) = PairingParamsVO.SessionProposeParams(
    relays = listOf(relay),
    proposer = SessionProposerVO(selfPublicKey.keyAsHex, metaData.toMetaDataVO()),
    chains, methods, events
)

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcResult.toEngineJsonRpcResult(): EngineDO.JsonRpcResponse.JsonRpcResult =
    EngineDO.JsonRpcResponse.JsonRpcResult(id = id, result = result.toString())

@JvmSynthetic
internal fun JsonRpcResponseVO.JsonRpcError.toEngineJsonRpcError(): EngineDO.JsonRpcResponse.JsonRpcError =
    EngineDO.JsonRpcResponse.JsonRpcError(id = id, error = EngineDO.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun PairingParamsVO.SessionProposeParams.toSessionApproveParams(selfPublicKey: PublicKey): SessionParamsVO.ApprovalParams =
    SessionParamsVO.ApprovalParams(
        relay = RelayProtocolOptionsVO(relays.first().protocol, relays.first().data),
        responderPublicKey = selfPublicKey.keyAsHex)

