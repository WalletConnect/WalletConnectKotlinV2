package com.walletconnect.walletconnectv2.client.model.mapper

import com.walletconnect.walletconnectv2.client.model.WalletConnectClientModel
import com.walletconnect.walletconnectv2.common.model.JsonRpcResponse
import com.walletconnect.walletconnectv2.engine.model.EngineModel
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.after.params.SessionPermissions
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.proposal.SessionProposedPermissions

internal fun EngineModel.SessionProposalDO.toClientSessionProposal(): WalletConnectClientModel.SessionProposal =
    WalletConnectClientModel.SessionProposal(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accounts)

internal fun WalletConnectClientModel.SessionProposal.toEngineSessionProposal(accountList: List<String>): EngineModel.SessionProposalDO =
    EngineModel.SessionProposalDO(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accountList)

internal fun EngineModel.SettledSession.toClientSettledSession(): WalletConnectClientModel.SettledSession =
    WalletConnectClientModel.SettledSession(
        topic,
        accounts,
        peerAppMetaData?.toClientAppMetaData(),
        permissions.toClientSettledSessionPermissions()
    )

internal fun EngineModel.SettledSession.Permissions.toClientSettledSessionPermissions(): WalletConnectClientModel.SettledSession.Permissions =
    WalletConnectClientModel.SettledSession.Permissions(
        blockchain.toClientSettledSessionBlockchain(),
        jsonRpc.toClientSettledSessionJsonRpc(),
        notifications.toClientSettledSessionNotifications()
    )

internal fun EngineModel.SettledSession.Permissions.Blockchain.toClientSettledSessionBlockchain(): WalletConnectClientModel.SettledSession.Permissions.Blockchain =
    WalletConnectClientModel.SettledSession.Permissions.Blockchain(chains)

internal fun EngineModel.SettledSession.Permissions.JsonRpc.toClientSettledSessionJsonRpc(): WalletConnectClientModel.SettledSession.Permissions.JsonRpc =
    WalletConnectClientModel.SettledSession.Permissions.JsonRpc(methods)

internal fun EngineModel.SettledSession.Permissions.Notifications.toClientSettledSessionNotifications(): WalletConnectClientModel.SettledSession.Permissions.Notifications =
    WalletConnectClientModel.SettledSession.Permissions.Notifications(types)

internal fun EngineModel.SessionRequest.toClientSessionRequest(): WalletConnectClientModel.SessionRequest =
    WalletConnectClientModel.SessionRequest(
        topic,
        chainId,
        WalletConnectClientModel.SessionRequest.JSONRPCRequest(request.id, request.method, request.params)
    )

internal fun WalletConnectClientModel.JsonRpcResponse.JsonRpcResult.toEngineRpcResult(): JsonRpcResponse.JsonRpcResult =
    JsonRpcResponse.JsonRpcResult(id, result)

internal fun WalletConnectClientModel.JsonRpcResponse.JsonRpcError.toEngineRpcError(): JsonRpcResponse.JsonRpcError =
    JsonRpcResponse.JsonRpcError(id, JsonRpcResponse.Error(error.code, error.message))

internal fun WalletConnectClientModel.SessionState.toEngineSessionState(): EngineModel.SessionState = EngineModel.SessionState(accounts)

internal fun WalletConnectClientModel.Notification.toEngineNotification(): EngineModel.Notification = EngineModel.Notification(type, data)

internal fun EngineModel.DeletedSession.toClientDeletedSession(): WalletConnectClientModel.DeletedSession =
    WalletConnectClientModel.DeletedSession(topic, reason)

internal fun EngineModel.SessionNotification.toClientSessionNotification(): WalletConnectClientModel.SessionNotification =
    WalletConnectClientModel.SessionNotification(topic, type, data)

internal fun WalletConnectClientModel.SessionPermissions.toEngineSessionPermissions(): EngineModel.SessionPermissions =
    EngineModel.SessionPermissions(
        blockchain?.chains?.let { chains -> EngineModel.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> EngineModel.Jsonrpc(methods) }
    )

internal fun EngineModel.SessionPermissions.toClientPerms(): WalletConnectClientModel.SessionPermissions =
    WalletConnectClientModel.SessionPermissions(
        blockchain?.chains?.let { chains -> WalletConnectClientModel.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> WalletConnectClientModel.Jsonrpc(methods) }
    )

internal fun EngineModel.SessionPermissions.toSessionsPermissions(): SessionPermissions =
    SessionPermissions(
        blockchain?.chains?.let { chains -> SessionProposedPermissions.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> SessionProposedPermissions.JsonRpc(methods) }
    )

internal fun WalletConnectClientModel.AppMetaData.toEngineAppMetaData() = EngineModel.AppMetaDataDO(name, description, url, icons)

internal fun EngineModel.AppMetaDataDO.toClientAppMetaData() = WalletConnectClientModel.AppMetaData(name, description, url, icons)