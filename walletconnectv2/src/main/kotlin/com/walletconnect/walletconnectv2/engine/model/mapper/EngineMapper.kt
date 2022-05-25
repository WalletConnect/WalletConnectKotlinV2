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
        requiredNamespaces = this.namespaces.toMapOfEngineNamespacesProposal(),
        proposerPublicKey = this.proposer.publicKey,
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
internal fun SessionVO.toEngineDOApprovedSessionVO(): EngineDO.Session =
    EngineDO.Session(
        topic,
        expiry,
        namespaces.toMapOfEngineNamespacesSession(),
        EngineDO.AppMetaData(
            peerMetaData?.name ?: String.Empty,
            peerMetaData?.description ?: String.Empty,
            peerMetaData?.url ?: String.Empty,
            peerMetaData?.icons?.map { iconUri -> iconUri } ?: listOf())
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionExtend(expiryVO: ExpiryVO): EngineDO.SessionExtend =
    EngineDO.SessionExtend(topic, expiryVO, namespaces.toMapOfEngineNamespacesSession(), selfMetaData?.toEngineDOAppMetaData())

@JvmSynthetic
private fun MetaDataVO.toEngineDOAppMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun PairingVO.toEngineDOSettledPairing(): EngineDO.PairingSettle =
    EngineDO.PairingSettle(topic, peerMetaData?.toEngineDOAppMetaData())

@JvmSynthetic
internal fun SessionVO.toSessionApproved(): EngineDO.SessionApproved =
    EngineDO.SessionApproved(
        topic = topic.value,
        peerAppMetaData = peerMetaData?.toEngineDOMetaData(),
        accounts = namespaces.flatMap { (_, namespace) -> namespace.accounts },
        namespaces = namespaces.toMapOfEngineNamespacesSession()
    )

@JvmSynthetic
internal fun PairingParamsVO.SessionProposeParams.toSessionSettleParams(
    selfParticipant: SessionParticipantVO,
    sessionExpiry: Long,
    namespaces: Map<String, EngineDO.Namespace.Session>,
): SessionParamsVO.SessionSettleParams =
    SessionParamsVO.SessionSettleParams(
        relay = RelayProtocolOptionsVO(relays.first().protocol, relays.first().data),
        controller = selfParticipant,
        namespaces = namespaces.toMapOfNamespacesVOSession(),
        expiry = sessionExpiry)

@JvmSynthetic
internal fun toSessionProposeParams(
    relays: List<EngineDO.RelayProtocolOptions>?,
    namespaces: Map<String, EngineDO.Namespace.Proposal>,
    selfPublicKey: PublicKey,
    metaData: EngineDO.AppMetaData,
) = PairingParamsVO.SessionProposeParams(
    relays = getSessionRelays(relays),
    proposer = SessionProposerVO(selfPublicKey.keyAsHex, metaData.toMetaDataVO()),
    namespaces = namespaces.toNamespacesVOProposal()
)

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Proposal>.toNamespacesVOProposal(): Map<String, NamespaceVO.Proposal> =
    this.mapValues { (_, namespace) ->
        NamespaceVO.Proposal(namespace.chains, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
            NamespaceVO.Proposal.Extension(extension.chains, extension.methods, extension.events)
        })
    }

@JvmSynthetic
internal fun Map<String, NamespaceVO.Proposal>.toMapOfEngineNamespacesProposal(): Map<String, EngineDO.Namespace.Proposal> =
    this.mapValues { (_, namespace) ->
        EngineDO.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
            EngineDO.Namespace.Proposal.Extension(extension.chains, extension.methods, extension.events)
        })
    }

@JvmSynthetic
internal fun Map<String, NamespaceVO.Session>.toMapOfEngineNamespacesSession(): Map<String, EngineDO.Namespace.Session> =
    this.mapValues { (_, namespaceVO) ->
        EngineDO.Namespace.Session(namespaceVO.accounts, namespaceVO.methods, namespaceVO.events, namespaceVO.extensions?.map { extension ->
            EngineDO.Namespace.Session.Extension(extension.accounts, extension.methods, extension.events)
        })
    }

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Session>.toMapOfNamespacesVOSession(): Map<String, NamespaceVO.Session> =
    this.mapValues { (_, namespace) ->
        NamespaceVO.Session(namespace.accounts, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
            NamespaceVO.Session.Extension(extension.accounts, extension.methods, extension.events)
        })
    }


@JvmSynthetic
internal fun getSessionRelays(relays: List<EngineDO.RelayProtocolOptions>?): List<RelayProtocolOptionsVO> = relays?.map { relay ->
    RelayProtocolOptionsVO(relay.protocol, relay.data)
} ?: listOf(RelayProtocolOptionsVO())

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
internal fun SessionParamsVO.SessionRequestParams.toEngineDORequest(topic: TopicVO): EngineDO.Request =
    EngineDO.Request(topic.value, request.method, request.params.toString(), chainId)

@JvmSynthetic
internal fun SessionParamsVO.EventParams.toEngineDOEvent(): EngineDO.Event =
    EngineDO.Event(event.name, event.data.toString(), chainId)