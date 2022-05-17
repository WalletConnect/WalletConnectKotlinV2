package com.walletconnect.walletconnectv2.client.mapper

import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.core.model.vo.sync.PendingRequestVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO

//TODO: Provide VO objects for engine classes. Remove using the EngineDO object in the client layer

@JvmSynthetic
internal fun EngineDO.ProposedSequence.toClientProposedSequence(): WalletConnect.Model.ProposedSequence =
    when (this) {
        is EngineDO.ProposedSequence.Pairing -> WalletConnect.Model.ProposedSequence.Pairing(this.uri)
        is EngineDO.ProposedSequence.Session -> WalletConnect.Model.ProposedSequence.Session
    }

@JvmSynthetic
internal fun WalletConnect.Model.JsonRpcResponse.toJsonRpcResponseVO(): JsonRpcResponseVO =
    when (this) {
        is WalletConnect.Model.JsonRpcResponse.JsonRpcResult -> this.toRpcResultVO()
        is WalletConnect.Model.JsonRpcResponse.JsonRpcError -> this.toRpcErrorVO()
    }

@JvmSynthetic
internal fun EngineDO.SettledSessionResponse.toClientSettledSessionResponse(): WalletConnect.Model.SettledSessionResponse =
    when (this) {
        is EngineDO.SettledSessionResponse.Result -> WalletConnect.Model.SettledSessionResponse.Result(settledSession.toClientSettledSession())
        is EngineDO.SettledSessionResponse.Error -> WalletConnect.Model.SettledSessionResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun EngineDO.SessionUpdateAccountsResponse.toClientUpdateSessionAccountsResponse(): WalletConnect.Model.SessionUpdateAccountsResponse =
    when (this) {
        is EngineDO.SessionUpdateAccountsResponse.Result -> WalletConnect.Model.SessionUpdateAccountsResponse.Result(topic.value, accounts)
        is EngineDO.SessionUpdateAccountsResponse.Error -> WalletConnect.Model.SessionUpdateAccountsResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun EngineDO.SessionUpdateNamespacesResponse.toClientUpdateSessionNamespacesResponse(): WalletConnect.Model.SessionUpdateResponse =
    when (this) {
        is EngineDO.SessionUpdateNamespacesResponse.Result ->
            WalletConnect.Model.SessionUpdateResponse.Result(topic.value, namespaces.toMapOfClientNamespacesSession())
        is EngineDO.SessionUpdateNamespacesResponse.Error -> WalletConnect.Model.SessionUpdateResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.toClientJsonRpcResponse(): WalletConnect.Model.JsonRpcResponse =
    when (this) {
        is EngineDO.JsonRpcResponse.JsonRpcResult -> this.toClientJsonRpcResult()
        is EngineDO.JsonRpcResponse.JsonRpcError -> this.toClientJsonRpcError()
    }

@JvmSynthetic
internal fun EngineDO.SessionProposal.toClientSessionProposal(): WalletConnect.Model.SessionProposal =
    WalletConnect.Model.SessionProposal(
        name,
        description,
        url,
        icons,
        requiredNamespaces.toMapOfClientNamespacesProposal(),
        proposerPublicKey,
        accounts,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun EngineDO.SessionRequest.toClientSessionRequest(): WalletConnect.Model.SessionRequest =
    WalletConnect.Model.SessionRequest(
        topic = topic,
        chainId = chainId,
        peerMetaData = peerAppMetaData?.toClientAppMetaData(),
        request = WalletConnect.Model.SessionRequest.JSONRPCRequest(
            id = request.id,
            method = request.method,
            params = request.params
        )
    )

@JvmSynthetic
internal fun WalletConnect.Model.JsonRpcResponse.JsonRpcResult.toRpcResultVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun WalletConnect.Model.JsonRpcResponse.JsonRpcError.toRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, error = JsonRpcResponseVO.Error(code, message))

@JvmSynthetic
internal fun WalletConnect.Model.SessionEvent.toEngineEvent(chainId: String): EngineDO.Event = EngineDO.Event(name, data, chainId)

@JvmSynthetic
internal fun EngineDO.SessionDelete.toClientDeletedSession(): WalletConnect.Model.DeletedSession =
    WalletConnect.Model.DeletedSession.Success(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionEvent.toClientSessionEvent(): WalletConnect.Model.SessionEvent =
    WalletConnect.Model.SessionEvent(name, data)

@JvmSynthetic
internal fun EngineDO.Session.toClientSettledSession(): WalletConnect.Model.Session =
    WalletConnect.Model.Session(topic.value,
        expiry.seconds,
        namespaces.toMapOfClientNamespacesSession(),
        peerAppMetaData?.toClientAppMetaData())

@JvmSynthetic
internal fun EngineDO.SessionUpdateExpiry.toClientSettledSession(): WalletConnect.Model.Session =
    WalletConnect.Model.Session(
        topic.value,
        expiry.seconds,
        namespaces.toMapOfClientNamespacesSession(),
        peerAppMetaData?.toClientAppMetaData()
    )

@JvmSynthetic
internal fun EngineDO.SessionRejected.toClientSessionRejected(): WalletConnect.Model.RejectedSession =
    WalletConnect.Model.RejectedSession(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionApproved.toClientSessionApproved(): WalletConnect.Model.ApprovedSession =
    WalletConnect.Model.ApprovedSession(topic, peerAppMetaData?.toClientAppMetaData(), namespaces.toMapOfClientNamespacesSession(), accounts)

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Session>.toMapOfClientNamespacesSession(): Map<String, WalletConnect.Model.Namespace.Session> = this.mapValues { (_, namespace) ->
    WalletConnect.Model.Namespace.Session(namespace.accounts, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
        WalletConnect.Model.Namespace.Session.Extension(extension.accounts, extension.methods, extension.events)
    })
}

@JvmSynthetic
internal fun WalletConnect.Model.AppMetaData.toEngineAppMetaData() = EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun EngineDO.AppMetaData.toClientAppMetaData() = WalletConnect.Model.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun WalletConnect.Params.Request.toEngineDORequest(): EngineDO.Request =
    EngineDO.Request(sessionTopic, method, params, chainId)

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.JsonRpcResult.toClientJsonRpcResult(): WalletConnect.Model.JsonRpcResponse.JsonRpcResult =
    WalletConnect.Model.JsonRpcResponse.JsonRpcResult(id, result)

@JvmSynthetic
internal fun EngineDO.SessionUpdateAccounts.toClientSessionsUpdateAccounts(): WalletConnect.Model.UpdatedSessionAccounts =
    WalletConnect.Model.UpdatedSessionAccounts(topic.value, accounts)

@JvmSynthetic
internal fun EngineDO.SessionUpdateNamespaces.toClientSessionsNamespaces(): WalletConnect.Model.UpdateSessionNamespaces =
    WalletConnect.Model.UpdateSessionNamespaces(topic.value, namespaces.toMapOfClientNamespacesSession())

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.JsonRpcError.toClientJsonRpcError(): WalletConnect.Model.JsonRpcResponse.JsonRpcError =
    WalletConnect.Model.JsonRpcResponse.JsonRpcError(id, code = error.code, message = error.message)

@JvmSynthetic
internal fun EngineDO.PairingSettle.toClientSettledPairing(): WalletConnect.Model.Pairing =
    WalletConnect.Model.Pairing(topic.value, metaData?.toClientAppMetaData())

@JvmSynthetic
internal fun List<PendingRequestVO>.mapToPendingRequests(): List<WalletConnect.Model.PendingRequest> = map { request ->
    WalletConnect.Model.PendingRequest(
        request.requestId,
        request.topic,
        request.method,
        request.chainId,
        request.params
    )
}

@JvmSynthetic
internal fun EngineDO.SessionPayloadResponse.toClientSessionPayloadResponse(): WalletConnect.Model.SessionRequestResponse =
    WalletConnect.Model.SessionRequestResponse(topic, chainId, method, result.toClientJsonRpcResponse())

@JvmSynthetic
internal fun Map<String, WalletConnect.Model.Namespace.Proposal>.toMapOfEngineNamespacesProposal(): Map<String, EngineDO.Namespace.Proposal> = mapValues { (_, namespace) ->
    EngineDO.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
        EngineDO.Namespace.Proposal.Extension(extension.chains, extension.methods, extension.events)
    })
}

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Proposal>.toMapOfClientNamespacesProposal(): Map<String, WalletConnect.Model.Namespace.Proposal> = mapValues { (_, namespace) ->
    WalletConnect.Model.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
        WalletConnect.Model.Namespace.Proposal.Extension(extension.chains, extension.methods, extension.events)
    })
}

@JvmSynthetic
internal fun Map<String, WalletConnect.Model.Namespace.Session>.toMapOfEngineNamespacesSession(): Map<String, EngineDO.Namespace.Session> = mapValues { (_, namespace) ->
    EngineDO.Namespace.Session(namespace.accounts, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
        EngineDO.Namespace.Session.Extension(extension.accounts, extension.methods, extension.events)
    })
}

@JvmSynthetic
internal fun List<WalletConnect.Model.RelayProtocolOptions>.toListEngineOfRelayProtocolOptions(): List<EngineDO.RelayProtocolOptions> = map { relayProtocolOptions ->
    EngineDO.RelayProtocolOptions(relayProtocolOptions.protocol, relayProtocolOptions.data)
}