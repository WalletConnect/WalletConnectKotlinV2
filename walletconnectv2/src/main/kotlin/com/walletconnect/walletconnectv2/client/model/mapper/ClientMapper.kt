package com.walletconnect.walletconnectv2.client.model.mapper

import com.walletconnect.walletconnectv2.client.model.WalletConnectClientModel
import com.walletconnect.walletconnectv2.common.model.vo.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.engine.model.EngineModel
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.after.params.SessionPermissions
import com.walletconnect.walletconnectv2.relay.model.clientsync.session.before.proposal.SessionProposedPermissions

internal fun EngineModel.SessionProposalDO.toClientSessionProposal(): WalletConnectClientModel.SessionProposal =
    WalletConnectClientModel.SessionProposal(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accounts)

internal fun WalletConnectClientModel.SessionProposal.toEngineSessionProposal(accountList: List<String>): EngineModel.SessionProposalDO =
    EngineModel.SessionProposalDO(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accountList)

internal fun EngineModel.SettledSessionDO.toClientSettledSession(): WalletConnectClientModel.SettledSession =
    WalletConnectClientModel.SettledSession(
        topic,
        accounts,
        peerAppMetaData?.toClientAppMetaData(),
        permissions.toClientSettledSessionPermissions()
    )

internal fun EngineModel.SettledSessionDO.PermissionsDO.toClientSettledSessionPermissions(): WalletConnectClientModel.SettledSession.Permissions =
    WalletConnectClientModel.SettledSession.Permissions(
        blockchain.toClientSettledSessionBlockchain(),
        jsonRpc.toClientSettledSessionJsonRpc(),
        notifications.toClientSettledSessionNotifications()
    )

internal fun EngineModel.SettledSessionDO.PermissionsDO.BlockchainDO.toClientSettledSessionBlockchain(): WalletConnectClientModel.SettledSession.Permissions.Blockchain =
    WalletConnectClientModel.SettledSession.Permissions.Blockchain(chains)

internal fun EngineModel.SettledSessionDO.PermissionsDO.JsonRpcDO.toClientSettledSessionJsonRpc(): WalletConnectClientModel.SettledSession.Permissions.JsonRpc =
    WalletConnectClientModel.SettledSession.Permissions.JsonRpc(methods)

internal fun EngineModel.SettledSessionDO.PermissionsDO.NotificationsDO.toClientSettledSessionNotifications(): WalletConnectClientModel.SettledSession.Permissions.Notifications =
    WalletConnectClientModel.SettledSession.Permissions.Notifications(types)

internal fun EngineModel.SessionRequestDO.toClientSessionRequest(): WalletConnectClientModel.SessionRequest =
    WalletConnectClientModel.SessionRequest(
        topic,
        chainId,
        WalletConnectClientModel.SessionRequest.JSONRPCRequest(request.id, request.method, request.params)
    )

internal fun WalletConnectClientModel.JsonRpcResponse.JsonRpcResult.toEngineRpcResult(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result)

internal fun WalletConnectClientModel.JsonRpcResponse.JsonRpcError.toEngineRpcError(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, JsonRpcResponseVO.Error(error.code, error.message))

internal fun WalletConnectClientModel.SessionState.toEngineSessionState(): EngineModel.SessionStateDO = EngineModel.SessionStateDO(accounts)

internal fun WalletConnectClientModel.Notification.toEngineNotification(): EngineModel.NotificationDO = EngineModel.NotificationDO(type, data)

internal fun EngineModel.DeletedSessionDO.toClientDeletedSession(): WalletConnectClientModel.DeletedSession =
    WalletConnectClientModel.DeletedSession(topic, reason)

internal fun EngineModel.SessionNotificationDO.toClientSessionNotification(): WalletConnectClientModel.SessionNotification =
    WalletConnectClientModel.SessionNotification(topic, type, data)

internal fun WalletConnectClientModel.SessionPermissions.toEngineSessionPermissions(): EngineModel.SessionPermissionsDO =
    EngineModel.SessionPermissionsDO(
        blockchain?.chains?.let { chains -> EngineModel.BlockchainDO(chains) },
        jsonRpc?.methods?.let { methods -> EngineModel.JsonRpcDO(methods) }
    )

internal fun EngineModel.SessionPermissionsDO.toClientPerms(): WalletConnectClientModel.SessionPermissions =
    WalletConnectClientModel.SessionPermissions(
        blockchain?.chains?.let { chains -> WalletConnectClientModel.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> WalletConnectClientModel.Jsonrpc(methods) }
    )

internal fun EngineModel.SessionPermissionsDO.toSessionsPermissions(): SessionPermissions =
    SessionPermissions(
        blockchain?.chains?.let { chains -> SessionProposedPermissions.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> SessionProposedPermissions.JsonRpc(methods) }
    )

internal fun WalletConnectClientModel.AppMetaData.toEngineAppMetaData() = EngineModel.AppMetaDataDO(name, description, url, icons)

internal fun EngineModel.AppMetaDataDO.toClientAppMetaData() = WalletConnectClientModel.AppMetaData(name, description, url, icons)