package com.walletconnect.sign.engine.model.mapper

import com.walletconnect.android_core.common.model.Expiry
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.core.exceptions.peer.PeerError
import com.walletconnect.sign.core.model.vo.PublicKey
import com.walletconnect.sign.core.model.vo.clientsync.common.MetaDataVO
import com.walletconnect.sign.core.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.walletconnect.sign.core.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.walletconnect.sign.core.model.vo.clientsync.pairing.payload.SessionProposerVO
import com.walletconnect.sign.core.model.vo.clientsync.session.params.SessionParamsVO
import com.walletconnect.android_core.common.model.vo.json_rpc.JsonRpcResponseVO
import com.walletconnect.sign.core.model.vo.sequence.PairingVO
import com.walletconnect.sign.core.model.vo.sequence.SessionVO
import com.walletconnect.android_core.common.model.vo.sync.WCRequestVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.ValidationError
import com.walletconnect.sign.util.Empty
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
    MetaDataVO(name, description, url, icons, RedirectVO(redirect))

@JvmSynthetic
internal fun MetaDataVO.toEngineDOMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons, redirect?.native)

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
            params = this.request.params
        )
    )

@JvmSynthetic
internal fun SessionParamsVO.DeleteParams.toEngineDoDeleteSession(topic: Topic): EngineDO.SessionDelete =
    EngineDO.SessionDelete(topic.value, message)

@JvmSynthetic
internal fun SessionParamsVO.EventParams.toEngineDOSessionEvent(topic: Topic): EngineDO.SessionEvent =
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
            peerMetaData?.icons?.map { iconUri -> iconUri } ?: listOf(),
            peerMetaData?.redirect?.native
        )
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionExtend(expiryVO: Expiry): EngineDO.SessionExtend =
    EngineDO.SessionExtend(topic, expiryVO, namespaces.toMapOfEngineNamespacesSession(), selfMetaData?.toEngineDOAppMetaData())

@JvmSynthetic
private fun MetaDataVO.toEngineDOAppMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons, redirect?.native)

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
internal fun SessionParamsVO.SessionRequestParams.toEngineDORequest(topic: Topic): EngineDO.Request =
    EngineDO.Request(topic.value, request.method, request.params, chainId)

@JvmSynthetic
internal fun SessionParamsVO.EventParams.toEngineDOEvent(): EngineDO.Event =
    EngineDO.Event(event.name, event.data.toString(), chainId)


@JvmSynthetic
internal fun ValidationError.toPeerError() = when (this) {
    is ValidationError.UnsupportedNamespaceKey -> PeerError.UnsupportedNamespaceKey(message)
    is ValidationError.UnsupportedChains -> PeerError.UnsupportedChains(message)
    is ValidationError.InvalidEvent -> PeerError.InvalidEvent(message)
    is ValidationError.InvalidExtendRequest -> PeerError.InvalidExtendRequest(message)
    is ValidationError.InvalidSessionRequest -> PeerError.InvalidMethod(message)
    is ValidationError.UnauthorizedEvent -> PeerError.UnauthorizedEvent(message)
    is ValidationError.UnauthorizedMethod -> PeerError.UnauthorizedMethod(message)
    is ValidationError.UserRejected -> PeerError.UserRejected(message)
    is ValidationError.UserRejectedEvents -> PeerError.UserRejectedEvents(message)
    is ValidationError.UserRejectedMethods -> PeerError.UserRejectedMethods(message)
    is ValidationError.UserRejectedChains -> PeerError.UserRejectedChains(message)
}