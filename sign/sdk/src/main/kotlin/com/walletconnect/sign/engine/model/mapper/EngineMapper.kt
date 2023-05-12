@file:JvmSynthetic

package com.walletconnect.sign.engine.model.mapper

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SessionProposer
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.PendingRequest
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.proposal.ProposalVO
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.ValidationError
import com.walletconnect.sign.json_rpc.model.JsonRpcMethod
import com.walletconnect.util.Empty
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
internal fun SignParams.SessionProposeParams.toEngineDO(topic: Topic): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        pairingTopic = topic.value,
        name = this.proposer.metadata.name,
        description = this.proposer.metadata.description,
        url = this.proposer.metadata.url,
        icons = this.proposer.metadata.icons.mapNotNull { convertToURI(it) },
        redirect = this.proposer.metadata.redirect?.native ?: String.Empty,
        requiredNamespaces = this.requiredNamespaces.toMapOfEngineNamespacesRequired(),
        optionalNamespaces = this.optionalNamespaces?.toMapOfEngineNamespacesOptional() ?: emptyMap(),
        properties = properties,
        proposerPublicKey = this.proposer.publicKey,
        relayProtocol = relays.first().protocol,
        relayData = relays.first().data
    )

@JvmSynthetic
internal fun SignParams.SessionProposeParams.toVO(topic: Topic, requestId: Long): ProposalVO =
    ProposalVO(
        requestId = requestId,
        pairingTopic = topic,
        name = proposer.metadata.name,
        description = proposer.metadata.description,
        url = proposer.metadata.url,
        icons = proposer.metadata.icons,
        redirect = proposer.metadata.redirect?.native ?: String.Empty,
        requiredNamespaces = requiredNamespaces,
        optionalNamespaces = optionalNamespaces ?: emptyMap(),
        properties = properties,
        proposerPublicKey = proposer.publicKey,
        relayProtocol = relays.first().protocol,
        relayData = relays.first().data
    )

@JvmSynthetic
internal fun ProposalVO.toSessionProposeRequest(): WCRequest =
    WCRequest(
        topic = pairingTopic,
        id = requestId,
        method = JsonRpcMethod.WC_SESSION_PROPOSE,
        params = SignParams.SessionProposeParams(
            relays = listOf(RelayProtocolOptions(protocol = relayProtocol, data = relayData)),
            proposer = SessionProposer(proposerPublicKey, AppMetaData(name, description, url, icons)),
            requiredNamespaces, optionalNamespaces, properties
        )
    )

@JvmSynthetic
internal fun SignParams.SessionRequestParams.toEngineDO(
    request: WCRequest,
    peerAppMetaData: AppMetaData?,
): EngineDO.SessionRequest =
    EngineDO.SessionRequest(
        topic = request.topic.value,
        chainId = chainId,
        peerAppMetaData = peerAppMetaData,
        request = EngineDO.SessionRequest.JSONRPCRequest(
            id = request.id,
            method = this.request.method,
            params = this.request.params
        )
    )

@JvmSynthetic
internal fun SignParams.DeleteParams.toEngineDO(topic: Topic): EngineDO.SessionDelete =
    EngineDO.SessionDelete(topic.value, message)

@JvmSynthetic
internal fun SignParams.EventParams.toEngineDO(topic: Topic): EngineDO.SessionEvent =
    EngineDO.SessionEvent(topic.value, event.name, event.data.toString(), chainId)

@JvmSynthetic
internal fun SessionVO.toEngineDO(): EngineDO.Session =
    EngineDO.Session(
        topic,
        expiry,
        pairingTopic,
        sessionNamespaces.toMapOfEngineNamespacesSession(),
        peerAppMetaData
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionExtend(expiryVO: Expiry): EngineDO.SessionExtend =
    EngineDO.SessionExtend(topic, expiryVO, pairingTopic, sessionNamespaces.toMapOfEngineNamespacesSession(), selfAppMetaData)


@JvmSynthetic
internal fun SessionVO.toSessionApproved(): EngineDO.SessionApproved =
    EngineDO.SessionApproved(
        topic = topic.value,
        peerAppMetaData = peerAppMetaData,
        accounts = sessionNamespaces.flatMap { (_, namespace) -> namespace.accounts },
        namespaces = sessionNamespaces.toMapOfEngineNamespacesSession()
    )

@JvmSynthetic
internal fun ProposalVO.toSessionSettleParams(
    selfParticipant: SessionParticipantVO,
    sessionExpiry: Long,
    namespaces: Map<String, EngineDO.Namespace.Session>,
): SignParams.SessionSettleParams =
    SignParams.SessionSettleParams(
        relay = RelayProtocolOptions(relayProtocol, relayData),
        controller = selfParticipant,
        namespaces = namespaces.toMapOfNamespacesVOSession(),
        expiry = sessionExpiry
    )

@JvmSynthetic
internal fun toSessionProposeParams(
    relays: List<RelayProtocolOptions>?,
    requiredNamespaces: Map<String, EngineDO.Namespace.Proposal>,
    optionalNamespaces: Map<String, EngineDO.Namespace.Proposal>,
    properties: Map<String, String>?,
    selfPublicKey: PublicKey,
    appMetaData: AppMetaData,
) = SignParams.SessionProposeParams(
    relays = relays ?: listOf(RelayProtocolOptions()),
    proposer = SessionProposer(selfPublicKey.keyAsHex, appMetaData),
    requiredNamespaces = requiredNamespaces.toNamespacesVORequired(),
    optionalNamespaces = optionalNamespaces.toNamespacesVOOptional(),
    properties = properties
)

@JvmSynthetic
internal fun ProposalVO.toEngineDO(): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        pairingTopic = pairingTopic.value,
        name = name,
        description = description,
        url = url,
        icons = icons.mapNotNull { convertToURI(it) },
        redirect = redirect,
        relayData = relayData,
        relayProtocol = relayProtocol,
        requiredNamespaces = requiredNamespaces.toMapOfEngineNamespacesRequired(),
        optionalNamespaces = optionalNamespaces.toMapOfEngineNamespacesOptional(),
        proposerPublicKey = proposerPublicKey,
        properties = properties
    )

private fun convertToURI(it: String) = try {
    URI(it)
} catch (e: Exception) {
    null
}

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Proposal>.toNamespacesVORequired(): Map<String, NamespaceVO.Proposal> =
    this.mapValues { (_, namespace) ->
        NamespaceVO.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Proposal>.toNamespacesVOOptional(): Map<String, NamespaceVO.Proposal> =
    this.mapValues { (_, namespace) ->
        NamespaceVO.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, NamespaceVO.Proposal>.toMapOfEngineNamespacesRequired(): Map<String, EngineDO.Namespace.Proposal> =
    this.mapValues { (_, namespace) ->
        EngineDO.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, NamespaceVO.Proposal>.toMapOfEngineNamespacesOptional(): Map<String, EngineDO.Namespace.Proposal> =
    this.mapValues { (_, namespace) ->
        EngineDO.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, NamespaceVO.Session>.toMapOfEngineNamespacesSession(): Map<String, EngineDO.Namespace.Session> =
    this.mapValues { (_, namespaceVO) ->
        EngineDO.Namespace.Session(namespaceVO.chains, namespaceVO.accounts, namespaceVO.methods, namespaceVO.events)
    }

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Session>.toMapOfNamespacesVOSession(): Map<String, NamespaceVO.Session> =
    this.mapValues { (_, namespace) ->
        NamespaceVO.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcResult.toEngineDO(): EngineDO.JsonRpcResponse.JsonRpcResult =
    EngineDO.JsonRpcResponse.JsonRpcResult(id = id, result = result.toString())

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcError.toEngineDO(): EngineDO.JsonRpcResponse.JsonRpcError =
    EngineDO.JsonRpcResponse.JsonRpcError(id = id, error = EngineDO.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun ProposalVO.toSessionApproveParams(selfPublicKey: PublicKey): CoreSignParams.ApprovalParams =
    CoreSignParams.ApprovalParams(
        relay = RelayProtocolOptions(relayProtocol, relayData),
        responderPublicKey = selfPublicKey.keyAsHex
    )

@JvmSynthetic
internal fun SignParams.SessionRequestParams.toEngineDO(topic: Topic): EngineDO.Request =
    EngineDO.Request(topic.value, request.method, request.params, chainId)

@JvmSynthetic
internal fun SignParams.EventParams.toEngineDOEvent(): EngineDO.Event =
    EngineDO.Event(event.name, event.data.toString(), chainId)

@JvmSynthetic
internal fun PendingRequest<String>.toSessionRequest(peerAppMetaData: AppMetaData?): EngineDO.SessionRequest =
    EngineDO.SessionRequest(topic.value, chainId, peerAppMetaData, EngineDO.SessionRequest.JSONRPCRequest(id, method, params))


@JvmSynthetic
internal fun ValidationError.toPeerError() = when (this) {
    is ValidationError.UnsupportedNamespaceKey -> PeerError.CAIP25.UnsupportedNamespaceKey(message)
    is ValidationError.UnsupportedChains -> PeerError.CAIP25.UnsupportedChains(message)
    is ValidationError.InvalidEvent -> PeerError.Invalid.Event(message)
    is ValidationError.InvalidExtendRequest -> PeerError.Invalid.ExtendRequest(message)
    is ValidationError.InvalidSessionRequest -> PeerError.Invalid.Method(message)
    is ValidationError.UnauthorizedEvent -> PeerError.Unauthorized.Event(message)
    is ValidationError.UnauthorizedMethod -> PeerError.Unauthorized.Method(message)
    is ValidationError.UserRejected -> PeerError.CAIP25.UserRejected(message)
    is ValidationError.UserRejectedEvents -> PeerError.CAIP25.UserRejectedEvents(message)
    is ValidationError.UserRejectedMethods -> PeerError.CAIP25.UserRejectedMethods(message)
    is ValidationError.UserRejectedChains -> PeerError.CAIP25.UserRejectedChains(message)
    is ValidationError.InvalidSessionProperties -> PeerError.CAIP25.InvalidSessionPropertiesObject(message)
    is ValidationError.EmptyNamespaces -> PeerError.CAIP25.EmptySessionNamespaces(message)
}