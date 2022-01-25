package com.walletconnect.walletconnectv2.client.mapper

import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.common.model.vo.JsonRpcResponseVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO

//TODO: Provide VO objects for engine classes. Remove using the EngineDO object in the client layer

@JvmSynthetic
internal fun EngineDO.SessionProposal.toClientSessionProposal(): WalletConnect.Model.SessionProposal =
    WalletConnect.Model.SessionProposal(
        name, description, url, icons, chains, methods, types, topic, publicKey, isController, ttl, accounts, relayProtocol
    )

@JvmSynthetic
internal fun WalletConnect.Model.SessionProposal.toEngineSessionProposal(accountList: List<String>): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name, description, url, icons, chains, methods, types, topic, proposerPublicKey, isController, ttl, accountList, relayProtocol
    )

@JvmSynthetic
internal fun EngineDO.SettledSession.toClientSettledSession(): WalletConnect.Model.SettledSession =
    WalletConnect.Model.SettledSession(
        topic.value, accounts, peerAppMetaData?.toClientAppMetaData(), permissions.toClientSettledSessionPermissions()
    )

@JvmSynthetic
internal fun EngineDO.SettledSession.Permissions.toClientSettledSessionPermissions(): WalletConnect.Model.SettledSession.Permissions =
    WalletConnect.Model.SettledSession.Permissions(
        blockchain.toClientSettledSessionBlockchain(),
        jsonRpc.toClientSettledSessionJsonRpc(),
        notifications.toClientSettledSessionNotifications()
    )

@JvmSynthetic
internal fun EngineDO.SettledSession.Permissions.Blockchain.toClientSettledSessionBlockchain(): WalletConnect.Model.SettledSession.Permissions.Blockchain =
    WalletConnect.Model.SettledSession.Permissions.Blockchain(chains)

@JvmSynthetic
internal fun EngineDO.SettledSession.Permissions.JsonRpc.toClientSettledSessionJsonRpc(): WalletConnect.Model.SettledSession.Permissions.JsonRpc =
    WalletConnect.Model.SettledSession.Permissions.JsonRpc(methods)

@JvmSynthetic
internal fun EngineDO.SettledSession.Permissions.Notifications.toClientSettledSessionNotifications(): WalletConnect.Model.SettledSession.Permissions.Notifications =
    WalletConnect.Model.SettledSession.Permissions.Notifications(types)

@JvmSynthetic
internal fun EngineDO.SessionRequest.toClientSessionRequest(): WalletConnect.Model.SessionRequest =
    WalletConnect.Model.SessionRequest(
        topic, chainId, WalletConnect.Model.SessionRequest.JSONRPCRequest(request.id, request.method, request.params)
    )

@JvmSynthetic
internal fun WalletConnect.Model.JsonRpcResponse.JsonRpcResult.toRpcResultVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun WalletConnect.Model.JsonRpcResponse.JsonRpcError.toRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, error = JsonRpcResponseVO.Error(error.code, error.message))

@JvmSynthetic
internal fun WalletConnect.Model.SessionState.toEngineSessionState(): EngineDO.SessionState = EngineDO.SessionState(accounts)

@JvmSynthetic
internal fun WalletConnect.Model.Notification.toEngineNotification(): EngineDO.Notification = EngineDO.Notification(type, data)

@JvmSynthetic
internal fun EngineDO.DeletedSession.toClientDeletedSession(): WalletConnect.Model.DeletedSession =
    WalletConnect.Model.DeletedSession(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionNotification.toClientSessionNotification(): WalletConnect.Model.SessionNotification =
    WalletConnect.Model.SessionNotification(topic, type, data)

@JvmSynthetic
internal fun EngineDO.SettledPairing.toClientSettledPairing(): WalletConnect.Model.SettledPairing =
    WalletConnect.Model.SettledPairing(topic.value, appMetaData?.toClientAppMetaData())

@JvmSynthetic
internal fun EngineDO.SessionRejected.toClientSessionRejected(): WalletConnect.Model.RejectedSession =
    WalletConnect.Model.RejectedSession(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionApproved.toClientSessionApproved(): WalletConnect.Model.ApprovedSession =
    WalletConnect.Model.ApprovedSession(topic, peerAppMetaData?.toClientAppMetaData(), permissions.toClientPerms())

@JvmSynthetic
internal fun WalletConnect.Model.SessionPermissions.toEngineSessionPermissions(): EngineDO.SessionPermissions =
    EngineDO.SessionPermissions(EngineDO.Blockchain(blockchain.chains), EngineDO.JsonRpc(jsonRpc.methods))

@JvmSynthetic
internal fun EngineDO.SessionPermissions.toClientPerms(): WalletConnect.Model.SessionPermissions =
    WalletConnect.Model.SessionPermissions(WalletConnect.Model.Blockchain(blockchain.chains), WalletConnect.Model.Jsonrpc(jsonRpc.methods))

@JvmSynthetic
internal fun WalletConnect.Model.AppMetaData.toEngineAppMetaData() = EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun EngineDO.AppMetaData.toClientAppMetaData() = WalletConnect.Model.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun WalletConnect.Model.JsonRpcResponse.toJsonRpcResponseVO(): JsonRpcResponseVO =
    when (this) {
        is WalletConnect.Model.JsonRpcResponse.JsonRpcResult -> this.toRpcResultVO()
        is WalletConnect.Model.JsonRpcResponse.JsonRpcError -> this.toRpcErrorVO()
    }

@JvmSynthetic
internal fun WalletConnect.Params.Request.toEngineDORequest(): EngineDO.Request =
    EngineDO.Request(sessionTopic, method, params, chainId)

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.JsonRpcResult.toClientJsonRpcResult(): WalletConnect.Model.JsonRpcResponse.JsonRpcResult =
    WalletConnect.Model.JsonRpcResponse.JsonRpcResult(id, result)