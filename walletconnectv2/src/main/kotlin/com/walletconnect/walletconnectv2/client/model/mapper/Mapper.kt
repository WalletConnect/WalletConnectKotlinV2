package com.walletconnect.walletconnectv2.client.model

import com.walletconnect.walletconnectv2.common.model.JsonRpcResponse
import com.walletconnect.walletconnectv2.engine.model.EngineData
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.after.params.SessionPermissions
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.proposal.SessionProposedPermissions

internal fun EngineData.SessionProposalDO.toClientSessionProposal(): WalletConnectClientData.SessionProposal =
    WalletConnectClientData.SessionProposal(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accounts)

internal fun WalletConnectClientData.SessionProposal.toEngineSessionProposal(accountList: List<String>): EngineData.SessionProposalDO =
    EngineData.SessionProposalDO(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accountList)

internal fun EngineData.SettledSession.toClientSettledSession(): WalletConnectClientData.SettledSession =
    WalletConnectClientData.SettledSession(topic, accounts, peerAppMetaData, permissions.toClientSettledSessionPermissions())

internal fun EngineData.SettledSession.Permissions.toClientSettledSessionPermissions(): WalletConnectClientData.SettledSession.Permissions =
    WalletConnectClientData.SettledSession.Permissions(
        blockchain.toClientSettledSessionBlockchain(),
        jsonRpc.toClientSettledSessionJsonRpc(),
        notifications.toClientSettledSessionNotifications()
    )

internal fun EngineData.SettledSession.Permissions.Blockchain.toClientSettledSessionBlockchain(): WalletConnectClientData.SettledSession.Permissions.Blockchain =
    WalletConnectClientData.SettledSession.Permissions.Blockchain(chains)

internal fun EngineData.SettledSession.Permissions.JsonRpc.toClientSettledSessionJsonRpc(): WalletConnectClientData.SettledSession.Permissions.JsonRpc =
    WalletConnectClientData.SettledSession.Permissions.JsonRpc(methods)

internal fun EngineData.SettledSession.Permissions.Notifications.toClientSettledSessionNotifications(): WalletConnectClientData.SettledSession.Permissions.Notifications =
    WalletConnectClientData.SettledSession.Permissions.Notifications(types)

internal fun EngineData.SessionRequest.toClientSessionRequest(): WalletConnectClientData.SessionRequest =
    WalletConnectClientData.SessionRequest(
        topic,
        chainId,
        WalletConnectClientData.SessionRequest.JSONRPCRequest(request.id, request.method, request.params)
    )

internal fun WalletConnectClientData.JsonRpcResponse.JsonRpcResult.toEngineRpcResult(): JsonRpcResponse.JsonRpcResult =
    JsonRpcResponse.JsonRpcResult(id, result)

internal fun WalletConnectClientData.JsonRpcResponse.JsonRpcError.toEngineRpcError(): JsonRpcResponse.JsonRpcError =
    JsonRpcResponse.JsonRpcError(id, JsonRpcResponse.Error(error.code, error.message))

internal fun WalletConnectClientData.SessionState.toEngineSessionState(): EngineData.SessionState = EngineData.SessionState(accounts)

internal fun WalletConnectClientData.Notification.toEngineNotification(): EngineData.Notification = EngineData.Notification(type, data)

internal fun EngineData.DeletedSession.toClientDeletedSession(): WalletConnectClientData.DeletedSession =
    WalletConnectClientData.DeletedSession(topic, reason)

internal fun EngineData.SessionNotification.toClientSessionNotification(): WalletConnectClientData.SessionNotification =
    WalletConnectClientData.SessionNotification(topic, type, data)

internal fun WalletConnectClientData.SessionPermissions.toEngineSessionPermissions(): EngineData.SessionPermissions =
    EngineData.SessionPermissions(
        blockchain?.chains?.let { chains -> EngineData.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> EngineData.Jsonrpc(methods) }
    )

internal fun EngineData.SessionPermissions.toClientPerms(): WalletConnectClientData.SessionPermissions =
    WalletConnectClientData.SessionPermissions(
        blockchain?.chains?.let { chains -> WalletConnectClientData.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> WalletConnectClientData.Jsonrpc(methods) }
    )

internal fun EngineData.SessionPermissions.toSessionsPermissions(): SessionPermissions =
    SessionPermissions(
        blockchain?.chains?.let { chains -> SessionProposedPermissions.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> SessionProposedPermissions.JsonRpc(methods) }
    )