@file:JvmSynthetic

package com.walletconnect.sign.engine.model.mapper

import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.vo.clientsync.common.NamespaceVO
import com.walletconnect.sign.common.model.vo.clientsync.common.SessionParticipantVO
import com.walletconnect.android.internal.common.model.SessionProposer
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParamsVO
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
internal fun SignParamsVO.SessionProposeParams.toEngineDO(): EngineDO.SessionProposal =
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
internal fun SignParamsVO.SessionRequestParams.toEngineDO(
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
internal fun SignParamsVO.DeleteParams.toEngineDO(topic: Topic): EngineDO.SessionDelete =
    EngineDO.SessionDelete(topic.value, message)

@JvmSynthetic
internal fun SignParamsVO.EventParams.toEngineDO(topic: Topic): EngineDO.SessionEvent =
    EngineDO.SessionEvent(topic.value, event.name, event.data.toString(), chainId)

@JvmSynthetic
internal fun SessionVO.toEngineDO(): EngineDO.Session =
    EngineDO.Session(
        topic,
        expiry,
        namespaces.toMapOfEngineNamespacesSession(),
        peerAppMetaData
    )

@JvmSynthetic
internal fun SessionVO.toEngineDOSessionExtend(expiryVO: Expiry): EngineDO.SessionExtend =
    EngineDO.SessionExtend(topic, expiryVO, namespaces.toMapOfEngineNamespacesSession(), selfAppMetaData)


@JvmSynthetic
internal fun SessionVO.toSessionApproved(): EngineDO.SessionApproved =
    EngineDO.SessionApproved(
        topic = topic.value,
        peerAppMetaData = peerAppMetaData,
        accounts = namespaces.flatMap { (_, namespace) -> namespace.accounts },
        namespaces = namespaces.toMapOfEngineNamespacesSession()
    )

@JvmSynthetic
internal fun SignParamsVO.SessionProposeParams.toSessionSettleParams(
    selfParticipant: SessionParticipantVO,
    sessionExpiry: Long,
    namespaces: Map<String, EngineDO.Namespace.Session>,
): SignParamsVO.SessionSettleParams =
    SignParamsVO.SessionSettleParams(
        relay = RelayProtocolOptions(relays.first().protocol, relays.first().data),
        controller = selfParticipant,
        namespaces = namespaces.toMapOfNamespacesVOSession(),
        expiry = sessionExpiry)

@JvmSynthetic
internal fun toSessionProposeParams(
    relays: List<RelayProtocolOptions>?,
    namespaces: Map<String, EngineDO.Namespace.Proposal>,
    selfPublicKey: PublicKey,
    appMetaData: AppMetaData,
) = SignParamsVO.SessionProposeParams(
    relays = relays ?: listOf(RelayProtocolOptions()),
    proposer = SessionProposer(selfPublicKey.keyAsHex, appMetaData),
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
internal fun JsonRpcResponse.JsonRpcResult.toEngineDO(): EngineDO.JsonRpcResponse.JsonRpcResult =
    EngineDO.JsonRpcResponse.JsonRpcResult(id = id, result = result.toString())

@JvmSynthetic
internal fun JsonRpcResponse.JsonRpcError.toEngineDO(): EngineDO.JsonRpcResponse.JsonRpcError =
    EngineDO.JsonRpcResponse.JsonRpcError(id = id, error = EngineDO.JsonRpcResponse.Error(error.code, error.message))

@JvmSynthetic
internal fun SignParamsVO.SessionProposeParams.toSessionApproveParams(selfPublicKey: PublicKey): SignParamsVO.ApprovalParams =
    SignParamsVO.ApprovalParams(
        relay = RelayProtocolOptions(relays.first().protocol, relays.first().data),
        responderPublicKey = selfPublicKey.keyAsHex)

@JvmSynthetic
internal fun SignParamsVO.SessionRequestParams.toEngineDO(topic: Topic): EngineDO.Request =
    EngineDO.Request(topic.value, request.method, request.params, chainId)

@JvmSynthetic
internal fun SignParamsVO.EventParams.toEngineDOEvent(): EngineDO.Event =
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
}