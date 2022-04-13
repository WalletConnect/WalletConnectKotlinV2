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
internal fun EngineDO.SessionUpdateMethodsResponse.toClientUpdateSessionMethodsResponse(): WalletConnect.Model.SessionUpdateMethodsResponse =
    when (this) {
        is EngineDO.SessionUpdateMethodsResponse.Result -> WalletConnect.Model.SessionUpdateMethodsResponse.Result(topic.value, methods)
        is EngineDO.SessionUpdateMethodsResponse.Error -> WalletConnect.Model.SessionUpdateMethodsResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun EngineDO.SessionUpdateEventsResponse.toClientUpdateSessionEventsResponse(): WalletConnect.Model.SessionUpdateEventsResponse =
    when (this) {
        is EngineDO.SessionUpdateEventsResponse.Result -> WalletConnect.Model.SessionUpdateEventsResponse.Result(topic.value, events)
        is EngineDO.SessionUpdateEventsResponse.Error -> WalletConnect.Model.SessionUpdateEventsResponse.Error(errorMessage)
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
        chains,
        methods,
        events,
        proposerPublicKey,
        accounts,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun WalletConnect.Model.SessionProposal.toEngineSessionProposal(accountList: List<String> = listOf()): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name,
        description,
        url,
        icons,
        chains,
        methods,
        events,
        proposerPublicKey,
        accountList,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun EngineDO.SessionRequest.toClientSessionRequest(): WalletConnect.Model.SessionRequest =
    WalletConnect.Model.SessionRequest(topic,
        chainId,
        WalletConnect.Model.SessionRequest.JSONRPCRequest(request.id, request.method, request.params))

@JvmSynthetic
internal fun WalletConnect.Model.JsonRpcResponse.JsonRpcResult.toRpcResultVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun WalletConnect.Model.JsonRpcResponse.JsonRpcError.toRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, error = JsonRpcResponseVO.Error(code, message))

@JvmSynthetic
internal fun WalletConnect.Model.SessionEvent.toEngineEvent(chainId: String?): EngineDO.Event = EngineDO.Event(name, data, chainId)

@JvmSynthetic
internal fun EngineDO.SessionDelete.toClientDeletedSession(): WalletConnect.Model.DeletedSession =
    WalletConnect.Model.DeletedSession.Success(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionEvent.toClientSessionEvent(): WalletConnect.Model.SessionEvent =
    WalletConnect.Model.SessionEvent(name, data)

@JvmSynthetic
internal fun EngineDO.Session.toClientSettledSession(): WalletConnect.Model.Session =
    WalletConnect.Model.Session(
        topic.value,
        expiry.seconds,
        accounts,
        methods,
        events,
        peerAppMetaData.toClientAppMetaData()
    )

@JvmSynthetic
internal fun EngineDO.SessionUpdateExpiry.toClientSettledSession(): WalletConnect.Model.Session =
    WalletConnect.Model.Session(
        topic.value,
        expiry.seconds,
        accounts,
        methods,
        events,
        peerAppMetaData.toClientAppMetaData()
    )

@JvmSynthetic
internal fun EngineDO.SessionRejected.toClientSessionRejected(): WalletConnect.Model.RejectedSession =
    WalletConnect.Model.RejectedSession(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionApproved.toClientSessionApproved(): WalletConnect.Model.ApprovedSession =
    WalletConnect.Model.ApprovedSession(topic, peerAppMetaData.toClientAppMetaData(), methods, events, accounts)

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
internal fun EngineDO.SessionUpdateMethods.toClientSessionsUpdateMethods(): WalletConnect.Model.UpdatedSessionMethods =
    WalletConnect.Model.UpdatedSessionMethods(topic.value, methods)

@JvmSynthetic
internal fun EngineDO.SessionUpdateEvents.toClientSessionsUpdateEvents(): WalletConnect.Model.UpdatedSessionEvents =
    WalletConnect.Model.UpdatedSessionEvents(topic.value, events)

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.JsonRpcError.toClientJsonRpcError(): WalletConnect.Model.JsonRpcResponse.JsonRpcError =
    WalletConnect.Model.JsonRpcResponse.JsonRpcError(id, code = error.code, message = error.message)

@JvmSynthetic
internal fun EngineDO.PairingSettle.toClientSettledPairing(): WalletConnect.Model.SettledPairing =
    WalletConnect.Model.SettledPairing(topic.value, metaData?.toClientAppMetaData())

@JvmSynthetic
internal fun List<PendingRequestVO>.mapToPendingRequests(): List<WalletConnect.Model.PendingRequest> =
    this.map { request ->
        WalletConnect.Model.PendingRequest(
            request.requestId,
            request.topic,
            request.method,
            request.chainId,
            request.params
        )
    }

@JvmSynthetic
internal fun EngineDO.SessionPayloadResponse.toClientSessionPayloadResponse(): WalletConnect.Model.SessionPayloadResponse =
    WalletConnect.Model.SessionPayloadResponse(topic, chainId, method, result.toClientJsonRpcResponse())