package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.core.model.vo.ExpiryVO
import com.walletconnect.walletconnectv2.core.model.vo.PublicKey
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.common.NamespaceVO
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
        namespaces = this.namespaces.toListOfEngineNamespaces(),
        proposerPublicKey = this.proposer.publicKey,
        accounts = listOf(),
        relayProtocol = relays.first().protocol,
        relayData = relays.first().data
    )

@JvmSynthetic
internal fun SessionParamsVO.SessionRequestParams.toEngineDOSessionRequest(
    request: WCRequestVO,
    peerMetaDataVO: MetaDataVO?,
): EngineDO.SessionRequest =
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
        topic, expiry, accounts, namespaces.toListOfEngineNamespaces(),
        EngineDO.AppMetaData(
            selfMetaData?.name ?: String.Empty,
            selfMetaData?.description ?: String.Empty,
            selfMetaData?.url ?: String.Empty,
            selfMetaData?.icons?.map { iconUri -> iconUri } ?: listOf())
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOApprovedSessionVO(): EngineDO.Session =
    EngineDO.Session(topic, expiry, accounts, namespaces.toListOfEngineNamespaces(), selfMetaData?.toEngineDOAppMetaData())

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionUpdateExpiry(expiryVO: ExpiryVO): EngineDO.SessionUpdateExpiry =
    EngineDO.SessionUpdateExpiry(topic, expiryVO, accounts, namespaces.toListOfEngineNamespaces(), selfMetaData?.toEngineDOAppMetaData())

@JvmSynthetic
private fun MetaDataVO.toEngineDOAppMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun PairingVO.toEngineDOSettledPairing(): EngineDO.PairingSettle =
    EngineDO.PairingSettle(topic, peerMetaData?.toEngineDOAppMetaData())

@JvmSynthetic
internal fun SessionVO.toSessionApproved(): EngineDO.SessionApproved =
    EngineDO.SessionApproved(topic.value, peerMetaData?.toEngineDOMetaData(), accounts, namespaces.toListOfEngineNamespaces())

@JvmSynthetic
internal fun PairingParamsVO.SessionProposeParams.toSessionSettleParams(
    selfParticipant: SessionParticipantVO,
    sessionExpiry: Long,
    accounts: List<String>,
    namespaces: List<EngineDO.Namespace>,
): SessionParamsVO.SessionSettleParams =
    SessionParamsVO.SessionSettleParams(
        relay = RelayProtocolOptionsVO(relays.first().protocol, relays.first().data),
        controller = selfParticipant,
        accounts = accounts,
        namespaces = namespaces.toNamespacesVO(),
        expiry = sessionExpiry)

@JvmSynthetic
internal fun toSessionProposeParams(
    relays: List<EngineDO.RelayProtocolOptions>?,
    namespaces: List<EngineDO.Namespace>,
    selfPublicKey: PublicKey,
    metaData: EngineDO.AppMetaData,
) = PairingParamsVO.SessionProposeParams(
    relays = getSessionRelays(relays),
    proposer = SessionProposerVO(selfPublicKey.keyAsHex, metaData.toMetaDataVO()),
    namespaces = namespaces.toNamespacesVO()
)

@JvmSynthetic
internal fun List<EngineDO.Namespace>.toNamespacesVO(): List<NamespaceVO> {
    return mutableListOf<NamespaceVO>().apply {
        this@toNamespacesVO.forEach { namespace ->
            add(NamespaceVO(namespace.chains, namespace.methods, namespace.events))
        }
    }
}

@JvmSynthetic
internal fun getSessionRelays(relays: List<EngineDO.RelayProtocolOptions>?): List<RelayProtocolOptionsVO> {
    if (relays == null) {
        return listOf(RelayProtocolOptionsVO())
    } else {
        return mutableListOf<RelayProtocolOptionsVO>().apply {
            relays.forEach { relay ->
                add(RelayProtocolOptionsVO(relay.protocol, relay.data))
            }
        }
    }
}

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

@JvmSynthetic
internal fun List<NamespaceVO>.toListOfEngineNamespaces(): List<EngineDO.Namespace> =
    mutableListOf<EngineDO.Namespace>().apply {
        this@toListOfEngineNamespaces.forEach { namespace ->
            add(namespace.toEngineNamespace())
        }
    }

@JvmSynthetic
internal fun NamespaceVO.toEngineNamespace(): EngineDO.Namespace = EngineDO.Namespace(chains, methods, events)

