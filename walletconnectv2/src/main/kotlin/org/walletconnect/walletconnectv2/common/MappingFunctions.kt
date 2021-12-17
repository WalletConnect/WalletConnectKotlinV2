@file:JvmName("MappingFunctions")

package org.walletconnect.walletconnectv2.common

import org.json.JSONObject
import org.walletconnect.walletconnectv2.client.WalletConnectClientData
import org.walletconnect.walletconnectv2.clientsync.pairing.Pairing
import org.walletconnect.walletconnectv2.clientsync.pairing.before.PreSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.pairing.before.proposal.*
import org.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingParticipant
import org.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingState
import org.walletconnect.walletconnectv2.clientsync.session.Session
import org.walletconnect.walletconnectv2.clientsync.session.after.params.SessionPermissions
import org.walletconnect.walletconnectv2.clientsync.session.before.proposal.SessionProposedPermissions
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.engine.model.EngineData
import org.walletconnect.walletconnectv2.jsonrpc.model.JsonRpcResponse
import org.walletconnect.walletconnectv2.jsonrpc.utils.JsonRpcMethod
import org.walletconnect.walletconnectv2.relay.waku.WakuNetworkRepository
import org.walletconnect.walletconnectv2.relay.walletconnect.WalletConnectRelayer
import java.net.URI
import kotlin.time.Duration

internal fun String.toPairProposal(): Pairing.Proposal {
    val properUriString = if (contains("wc://")) this else replace("wc:", "wc://")
    val pairUri = URI(properUriString)
    val mapOfQueryParameters: Map<String, String> =
        pairUri.query.split("&")
            .associate { query -> query.substringBefore("=") to query.substringAfter("=") }
    val relay = JSONObject(mapOfQueryParameters["relay"] ?: "{}")
    val publicKey = mapOfQueryParameters["publicKey"] ?: ""
    val controller: Boolean = mapOfQueryParameters["controller"].toBoolean()
    val ttl: Long = Duration.days(30).inWholeSeconds

    return Pairing.Proposal(
        topic = Topic(pairUri.userInfo),
        relay = relay,
        pairingProposer = PairingProposer(publicKey, controller),
        pairingSignal = PairingSignal("uri", PairingSignalParams(properUriString)),
        permissions = PairingProposedPermissions(JsonRPC(listOf(JsonRpcMethod.WC_SESSION_PROPOSE))),
        ttl = Ttl(ttl)
    )
}

internal fun Pairing.Proposal.toPairingSuccess(settleTopic: Topic, expiry: Expiry, selfPublicKey: PublicKey): Pairing.Success =
    Pairing.Success(
        settledTopic = settleTopic,
        relay = relay,
        responder = PairingParticipant(publicKey = selfPublicKey.keyAsHex),
        expiry = expiry,
        state = PairingState(null)
    )

internal fun Pairing.Proposal.toApprove(
    id: Long,
    settleTopic: Topic,
    expiry: Expiry,
    selfPublicKey: PublicKey
): PreSettlementPairing.Approve = PreSettlementPairing.Approve(id = id, params = this.toPairingSuccess(settleTopic, expiry, selfPublicKey))

internal fun Session.Proposal.toSessionProposal(): EngineData.SessionProposal =
    EngineData.SessionProposal(
        name = this.proposer.metadata?.name!!,
        description = this.proposer.metadata.description,
        url = this.proposer.metadata.url,
        icons = this.proposer.metadata.icons.map { URI(it) },
        chains = this.permissions.blockchain.chains,
        methods = this.permissions.jsonRpc.methods,
        types = this.permissions.notifications.types,
        topic = this.topic.value,
        proposerPublicKey = this.proposer.publicKey,
        ttl = this.ttl.seconds,
        accounts = listOf()
    )

internal fun WalletConnectRelayer.RelayFactory.toWakuNetworkInitParams(): WakuNetworkRepository.WakuNetworkFactory =
    WakuNetworkRepository.WakuNetworkFactory(useTls, hostName, projectId, application)

internal fun EngineData.SessionProposal.toClientSessionProposal(): WalletConnectClientData.SessionProposal =
    WalletConnectClientData.SessionProposal(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accounts)

internal fun WalletConnectClientData.SessionProposal.toEngineSessionProposal(accountList: List<String>): EngineData.SessionProposal =
    EngineData.SessionProposal(name, description, url, icons, chains, methods, types, topic, proposerPublicKey, ttl, accountList)

internal fun EngineData.SettledSession.toClientSettledSession(): WalletConnectClientData.SettledSession =
    WalletConnectClientData.SettledSession(topic, accounts, peerAppMetaData, permissions.toClientSettledSessionPermissions())

private fun EngineData.SettledSession.Permissions.toClientSettledSessionPermissions(): WalletConnectClientData.SettledSession.Permissions =
    WalletConnectClientData.SettledSession.Permissions(blockchain.toClientSettledSessionBlockchain(), jsonRpc.toClientSettledSessionJsonRpc(), notifications.toClientSettledSessionNotifications())

private fun EngineData.SettledSession.Permissions.Blockchain.toClientSettledSessionBlockchain(): WalletConnectClientData.SettledSession.Permissions.Blockchain =
    WalletConnectClientData.SettledSession.Permissions.Blockchain(chains)

private fun EngineData.SettledSession.Permissions.JsonRpc.toClientSettledSessionJsonRpc(): WalletConnectClientData.SettledSession.Permissions.JsonRpc =
    WalletConnectClientData.SettledSession.Permissions.JsonRpc(methods)

private fun EngineData.SettledSession.Permissions.Notifications.toClientSettledSessionNotifications(): WalletConnectClientData.SettledSession.Permissions.Notifications =
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

internal fun WalletConnectClientData.Notification.toEngineNotification(): EngineData.Notification =
    EngineData.Notification(type, data)

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