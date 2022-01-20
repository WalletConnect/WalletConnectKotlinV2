package com.walletconnect.walletconnectv2.client.mapper

import com.walletconnect.walletconnectv2.client.*
import com.walletconnect.walletconnectv2.common.model.vo.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO

//TODO: Provide VO objects for engine classes. Remove using the EngineDO object in the client layer

internal fun EngineDO.SessionProposal.toClientSessionProposal(): WalletConnect.Model.SessionProposal =
    WalletConnect.Model.SessionProposal(
        name, description, url, icons, chains, methods, types, topic, publicKey, isController, ttl, accounts, relayProtocol
    )

internal fun WalletConnect.Model.SessionProposal.toEngineSessionProposal(accountList: List<String>): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name, description, url, icons, chains, methods, types, topic, proposerPublicKey, isController, ttl, accountList, relayProtocol
    )

internal fun EngineDO.SettledSession.toClientSettledSession(): WalletConnect.Model.SettledSession =
    WalletConnect.Model.SettledSession(
        topic.value, accounts, peerAppMetaData?.toClientAppMetaData(), permissions.toClientSettledSessionPermissions()
    )

internal fun EngineDO.SettledSession.Permissions.toClientSettledSessionPermissions(): WalletConnect.Model.SettledSession.Permissions =
    WalletConnect.Model.SettledSession.Permissions(
        blockchain.toClientSettledSessionBlockchain(),
        jsonRpc.toClientSettledSessionJsonRpc(),
        notifications.toClientSettledSessionNotifications()
    )

internal fun EngineDO.SettledSession.Permissions.Blockchain.toClientSettledSessionBlockchain(): WalletConnect.Model.SettledSession.Permissions.Blockchain =
    WalletConnect.Model.SettledSession.Permissions.Blockchain(chains)

internal fun EngineDO.SettledSession.Permissions.JsonRpc.toClientSettledSessionJsonRpc(): WalletConnect.Model.SettledSession.Permissions.JsonRpc =
    WalletConnect.Model.SettledSession.Permissions.JsonRpc(methods)

internal fun EngineDO.SettledSession.Permissions.Notifications.toClientSettledSessionNotifications(): WalletConnect.Model.SettledSession.Permissions.Notifications =
    WalletConnect.Model.SettledSession.Permissions.Notifications(types)

internal fun EngineDO.SessionRequest.toClientSessionRequest(): WalletConnect.Model.SessionRequest =
    WalletConnect.Model.SessionRequest(
        topic, chainId, WalletConnect.Model.SessionRequest.JSONRPCRequest(request.id, request.method, request.params)
    )

internal fun WalletConnect.Model.JsonRpcResponse.JsonRpcResult.toRpcResultVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result = result)

internal fun WalletConnect.Model.JsonRpcResponse.JsonRpcError.toRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, error = JsonRpcResponseVO.Error(error.code, error.message))

internal fun WalletConnect.Model.SessionState.toEngineSessionState(): EngineDO.SessionState = EngineDO.SessionState(accounts)

internal fun WalletConnect.Model.Notification.toEngineNotification(): EngineDO.Notification = EngineDO.Notification(type, data)

internal fun EngineDO.DeletedSession.toClientDeletedSession(): WalletConnect.Model.DeletedSession =
    WalletConnect.Model.DeletedSession(topic, reason)

internal fun EngineDO.SessionNotification.toClientSessionNotification(): WalletConnect.Model.SessionNotification =
    WalletConnect.Model.SessionNotification(topic, type, data)

internal fun EngineDO.SettledPairing.toClientSettledPairing(): WalletConnect.Model.SettledPairing =
    WalletConnect.Model.SettledPairing(
        topic.value, permissions = WalletConnect.Model.SessionPermissions(
            WalletConnect.Model.Blockchain(permissions.blockchain?.chains ?: emptyList()),
            WalletConnect.Model.Jsonrpc(permissions.jsonRpc?.methods ?: emptyList())
        )
    )

internal fun EngineDO.SessionRejected.toClientSessionRejected(): WalletConnect.Model.RejectedSession =
    WalletConnect.Model.RejectedSession(topic, reason)

internal fun EngineDO.SessionApproved.toClientSessionApproved(): WalletConnect.Model.ApprovedSession =
    WalletConnect.Model.ApprovedSession(topic, peerAppMetaData?.toClientAppMetaData(), permissions.toClientPerms())

internal fun WalletConnect.Model.SessionPermissions.toEngineSessionPermissions(): EngineDO.SessionPermissions =
    EngineDO.SessionPermissions(
        blockchain?.chains?.let { chains -> EngineDO.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> EngineDO.JsonRpc(methods) }
    )

internal fun EngineDO.SessionPermissions.toClientPerms(): WalletConnect.Model.SessionPermissions =
    WalletConnect.Model.SessionPermissions(
        blockchain?.chains?.let { chains -> WalletConnect.Model.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> WalletConnect.Model.Jsonrpc(methods) })

internal fun WalletConnect.Model.AppMetaData.toEngineAppMetaData() = EngineDO.AppMetaData(name, description, url, icons)

internal fun EngineDO.AppMetaData.toClientAppMetaData() = WalletConnect.Model.AppMetaData(name, description, url, icons)

internal fun WalletConnect.Model.JsonRpcResponse.toJsonRpcResponseVO(): JsonRpcResponseVO =
    when (this) {
        is WalletConnect.Model.JsonRpcResponse.JsonRpcResult -> this.toRpcResultVO()
        is WalletConnect.Model.JsonRpcResponse.JsonRpcError -> this.toRpcErrorVO()
    }