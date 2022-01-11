package com.walletconnect.walletconnectv2.client.mapper

import com.walletconnect.walletconnectv2.client.*
import com.walletconnect.walletconnectv2.common.model.vo.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO

//TODO: Provide VO objects for engine classes. Remove using the EngineDO object in the client layer

internal fun EngineDO.SessionProposal.toClientSessionProposal(): SessionProposal =
    SessionProposal(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accounts)

internal fun SessionProposal.toEngineSessionProposal(accountList: List<String>): EngineDO.SessionProposal =
    EngineDO.SessionProposal(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accountList)

internal fun EngineDO.SettledSession.toClientSettledSession(): SettledSession =
    SettledSession(topic.value, accounts, peerAppMetaData?.toClientAppMetaData(), permissions.toClientSettledSessionPermissions())

internal fun EngineDO.SettledSession.Permissions.toClientSettledSessionPermissions(): SettledSession.Permissions =
    SettledSession.Permissions(
        blockchain.toClientSettledSessionBlockchain(),
        jsonRpc.toClientSettledSessionJsonRpc(),
        notifications.toClientSettledSessionNotifications()
    )

internal fun EngineDO.SettledSession.Permissions.Blockchain.toClientSettledSessionBlockchain(): SettledSession.Permissions.Blockchain =
    SettledSession.Permissions.Blockchain(chains)

internal fun EngineDO.SettledSession.Permissions.JsonRpc.toClientSettledSessionJsonRpc(): SettledSession.Permissions.JsonRpc =
    SettledSession.Permissions.JsonRpc(methods)

internal fun EngineDO.SettledSession.Permissions.Notifications.toClientSettledSessionNotifications(): SettledSession.Permissions.Notifications =
    SettledSession.Permissions.Notifications(types)

internal fun EngineDO.SessionRequest.toClientSessionRequest(): SessionRequest =
    SessionRequest(topic, chainId, SessionRequest.JSONRPCRequest(request.id, request.method, request.params))

internal fun JsonRpcResponse.JsonRpcResult.toRpcResultVO(): JsonRpcResponseVO.JsonRpcResult = JsonRpcResponseVO.JsonRpcResult(id, result)

internal fun JsonRpcResponse.JsonRpcError.toRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, JsonRpcResponseVO.Error(error.code, error.message))

internal fun SessionState.toEngineSessionState(): EngineDO.SessionState = EngineDO.SessionState(accounts)

internal fun Notification.toEngineNotification(): EngineDO.Notification = EngineDO.Notification(type, data)

internal fun EngineDO.DeletedSession.toClientDeletedSession(): DeletedSession = DeletedSession(topic, reason)

internal fun EngineDO.SessionNotification.toClientSessionNotification(): SessionNotification = SessionNotification(topic, type, data)

internal fun SessionPermissions.toEngineSessionPermissions(): EngineDO.SessionPermissions =
    EngineDO.SessionPermissions(
        blockchain?.chains?.let { chains -> EngineDO.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> EngineDO.JsonRpc(methods) }
    )

internal fun EngineDO.SessionPermissions.toClientPerms(): SessionPermissions =
    SessionPermissions(blockchain?.chains?.let { chains -> Blockchain(chains) }, jsonRpc?.methods?.let { methods -> Jsonrpc(methods) })

internal fun AppMetaData.toEngineAppMetaData() = EngineDO.AppMetaData(name, description, url, icons)

internal fun EngineDO.AppMetaData.toClientAppMetaData() = AppMetaData(name, description, url, icons)

internal fun JsonRpcResponse.toJsonRpcResponseVO(): JsonRpcResponseVO =
    when (this) {
        is JsonRpcResponse.JsonRpcResult -> this.toRpcResultVO()
        is JsonRpcResponse.JsonRpcError -> this.toRpcErrorVO()
    }