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
internal fun EngineDO.SessionUpgradeResponse.toClientUpgradedSessionResponse(): WalletConnect.Model.SessionUpgradeResponse =
    when (this) {
        is EngineDO.SessionUpgradeResponse.Result ->
            WalletConnect.Model.SessionUpgradeResponse.Result(topic.value,
                WalletConnect.Model.SessionPermissions(
                    WalletConnect.Model.SessionPermissions.JsonRpc(methods),
                    if (types != null) WalletConnect.Model.SessionPermissions.Notifications(types) else null
                )
            )

        is EngineDO.SessionUpgradeResponse.Error -> WalletConnect.Model.SessionUpgradeResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun EngineDO.SessionUpdateResponse.toClientUpdateSessionResponse(): WalletConnect.Model.SessionUpdateResponse =
    when (this) {
        is EngineDO.SessionUpdateResponse.Result -> WalletConnect.Model.SessionUpdateResponse.Result(topic.value, accounts)
        is EngineDO.SessionUpdateResponse.Error -> WalletConnect.Model.SessionUpdateResponse.Error(errorMessage)
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
        types,
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
        if (types?.isEmpty() == true) null else types,
        proposerPublicKey,
        accountList,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toClientSettledSessionPermissions(): WalletConnect.Model.SessionPermissions =
    WalletConnect.Model.SessionPermissions(
        jsonRpc.toClientSettledSessionJsonRpc(),
        notifications?.toClientSettledSessionNotifications()
    )

@JvmSynthetic
internal fun EngineDO.SessionPermissions.JsonRpc.toClientSettledSessionJsonRpc(): WalletConnect.Model.SessionPermissions.JsonRpc =
    WalletConnect.Model.SessionPermissions.JsonRpc(methods)

@JvmSynthetic
internal fun EngineDO.SessionPermissions.Notifications.toClientSettledSessionNotifications(): WalletConnect.Model.SessionPermissions.Notifications =
    WalletConnect.Model.SessionPermissions.Notifications(types)

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
internal fun WalletConnect.Model.SessionState.toEngineSessionState(): EngineDO.SessionState = EngineDO.SessionState(accounts)

@JvmSynthetic
internal fun WalletConnect.Model.Notification.toEngineNotification(): EngineDO.Notification = EngineDO.Notification(type, data)

@JvmSynthetic
internal fun EngineDO.SessionDelete.toClientDeletedSession(): WalletConnect.Model.DeletedSession =
    WalletConnect.Model.DeletedSession.Success(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionNotification.toClientSessionNotification(): WalletConnect.Model.SessionNotification =
    WalletConnect.Model.SessionNotification(topic, type, data)

@JvmSynthetic
internal fun EngineDO.Session.toClientSettledSession(): WalletConnect.Model.Session =
    WalletConnect.Model.Session(
        topic.value,
        expiry.seconds,
        accounts,
        peerAppMetaData?.toClientAppMetaData(),
        permissions.toClientSettledSessionPermissions()
    )

@JvmSynthetic
internal fun EngineDO.SessionExtend.toClientSettledSession(): WalletConnect.Model.Session =
    WalletConnect.Model.Session(
        topic.value,
        expiry.seconds,
        accounts,
        peerAppMetaData?.toClientAppMetaData(),
        permissions.toClientSettledSessionPermissions()
    )

@JvmSynthetic
internal fun EngineDO.SessionRejected.toClientSessionRejected(): WalletConnect.Model.RejectedSession =
    WalletConnect.Model.RejectedSession(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionApproved.toClientSessionApproved(): WalletConnect.Model.ApprovedSession =
    WalletConnect.Model.ApprovedSession(topic, peerAppMetaData?.toClientAppMetaData(), permissions.toClientPerms(), accounts)

@JvmSynthetic
internal fun WalletConnect.Model.SessionPermissions.toEngineSessionPermissions(): EngineDO.SessionPermissions =
    EngineDO.SessionPermissions(
        EngineDO.SessionPermissions.JsonRpc(jsonRpc.methods),
        if (notification != null) EngineDO.SessionPermissions.Notifications(notification.types) else null
    )

@JvmSynthetic
internal fun WalletConnect.Model.Blockchain.toEngineBlockchain(): EngineDO.Blockchain = EngineDO.Blockchain(chains)

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toClientPerms(): WalletConnect.Model.SessionPermissions =
    WalletConnect.Model.SessionPermissions(
        WalletConnect.Model.SessionPermissions.JsonRpc(jsonRpc.methods),
        if (notifications != null) WalletConnect.Model.SessionPermissions.Notifications(notifications.types) else null
    )

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
internal fun EngineDO.JsonRpcResponse.JsonRpcError.toClientJsonRpcError(): WalletConnect.Model.JsonRpcResponse.JsonRpcError =
    WalletConnect.Model.JsonRpcResponse.JsonRpcError(id, code = error.code, message = error.message)

@JvmSynthetic
internal fun EngineDO.SessionUpdate.toClientSessionsUpdate(): WalletConnect.Model.UpdatedSession =
    WalletConnect.Model.UpdatedSession(topic.value, accounts)

@JvmSynthetic
internal fun EngineDO.PairingSettle.toClientSettledPairing(): WalletConnect.Model.SettledPairing =
    WalletConnect.Model.SettledPairing(topic.value, metaData?.toClientAppMetaData())

@JvmSynthetic
internal fun EngineDO.SessionUpgrade.toClientSessionsUpgrade(): WalletConnect.Model.UpgradedSession =
    WalletConnect.Model.UpgradedSession(
        topic.value,
        WalletConnect.Model.SessionPermissions(
            WalletConnect.Model.SessionPermissions.JsonRpc(methods),
            WalletConnect.Model.SessionPermissions.Notifications(types))
    )

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