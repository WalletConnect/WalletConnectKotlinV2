@file:JvmSynthetic

package com.walletconnect.sign.engine.model.mapper

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.*
import com.walletconnect.android.internal.common.model.params.CoreSignParams
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.sequence.SessionVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.ValidationError
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
        icons = this.proposer.metadata.icons.map { URI(it) },
        requiredNamespaces = this.requiredNamespaces.toMapOfEngineNamespacesRequired(),
        optionalNamespaces = this.optionalNamespaces.toMapOfEngineNamespacesOptional(),
        properties = properties,
        proposerPublicKey = this.proposer.publicKey,
        relayProtocol = relays.first().protocol,
        relayData = relays.first().data
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
        sessionNamespaces.toMapOfEngineNamespacesSession(),
        peerAppMetaData
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionExtend(expiryVO: Expiry): EngineDO.SessionExtend =
    EngineDO.SessionExtend(topic, expiryVO, sessionNamespaces.toMapOfEngineNamespacesSession(), selfAppMetaData)


@JvmSynthetic
internal fun SessionVO.toSessionApproved(): EngineDO.SessionApproved =
    EngineDO.SessionApproved(
        topic = topic.value,
        peerAppMetaData = peerAppMetaData,
        accounts = sessionNamespaces.flatMap { (_, namespace) -> namespace.accounts },
        namespaces = sessionNamespaces.toMapOfEngineNamespacesSession()
    )

@JvmSynthetic
internal fun SignParams.SessionProposeParams.toSessionSettleParams(
    selfParticipant: SessionParticipantVO,
    sessionExpiry: Long,
    namespaces: Map<String, EngineDO.Namespace.Session>,
): SignParams.SessionSettleParams =
    SignParams.SessionSettleParams(
        relay = RelayProtocolOptions(relays.first().protocol, relays.first().data),
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
internal fun Map<String, EngineDO.Namespace.Proposal>.toNamespacesVORequired(): Map<String, NamespaceVO.Required> =
    this.mapValues { (_, namespace) ->
        NamespaceVO.Required(namespace.chains, namespace.methods, namespace.events, namespace.rpcDocuments, namespace.rpcEndpoints)
    }

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Proposal>.toNamespacesVOOptional(): Map<String, NamespaceVO.Optional> =
    this.mapValues { (_, namespace) ->
        NamespaceVO.Optional(namespace.chains, namespace.methods, namespace.events, namespace.rpcDocuments, namespace.rpcEndpoints)
    }

@JvmSynthetic
internal fun Map<String, NamespaceVO.Required>.toMapOfEngineNamespacesRequired(): Map<String, EngineDO.Namespace.Proposal> =
    this.mapValues { (_, namespace) ->
        EngineDO.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events, namespace.rpcDocuments, namespace.rpcEndpoints)
    }

@JvmSynthetic
internal fun Map<String, NamespaceVO.Optional>.toMapOfEngineNamespacesOptional(): Map<String, EngineDO.Namespace.Proposal> =
    this.mapValues { (_, namespace) ->
        EngineDO.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events, namespace.rpcDocuments, namespace.rpcEndpoints)
    }

@JvmSynthetic
internal fun Map<String, NamespaceVO.Session>.toMapOfEngineNamespacesSession(): Map<String, EngineDO.Namespace.Session> =
    this.mapValues { (_, namespaceVO) ->
        EngineDO.Namespace.Session(
            namespaceVO.chains,
            namespaceVO.accounts,
            namespaceVO.methods,
            namespaceVO.events,
            namespaceVO.rpcDocuments,
            namespaceVO.rpcEndpoints
        )
    }

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Session>.toMapOfNamespacesVOSession(): Map<String, NamespaceVO.Session> =
    this.mapValues { (_, namespace) ->
        NamespaceVO.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events, namespace.rpcDocuments, namespace.rpcEndpoints)
    }

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcResult.toEngineDO(): EngineDO.JsonRpcResponse.JsonRpcResult =
    EngineDO.JsonRpcResponse.JsonRpcResult(id = id, result = result.toString())

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcError.toEngineDO(): EngineDO.JsonRpcResponse.JsonRpcError =
    EngineDO.JsonRpcResponse.JsonRpcError(id = id, error = EngineDO.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun SignParams.SessionProposeParams.toSessionApproveParams(selfPublicKey: PublicKey): CoreSignParams.ApprovalParams =
    CoreSignParams.ApprovalParams(
        relay = RelayProtocolOptions(relays.first().protocol, relays.first().data),
        responderPublicKey = selfPublicKey.keyAsHex
    )

@JvmSynthetic
internal fun SignParams.SessionRequestParams.toEngineDO(topic: Topic): EngineDO.Request =
    EngineDO.Request(topic.value, request.method, request.params, chainId)

@JvmSynthetic
internal fun SignParams.EventParams.toEngineDOEvent(): EngineDO.Event =
    EngineDO.Event(event.name, event.data.toString(), chainId)


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
    is ValidationError.InvalidRpcEndpoint -> PeerError.CAIP25.MalformedRpcEndpointUrl(message)
    is ValidationError.InvalidRpcDocument -> PeerError.CAIP25.MalformedRpcDocumentUrl(message)
}