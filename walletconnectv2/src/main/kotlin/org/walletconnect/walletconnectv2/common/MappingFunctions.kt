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
import org.walletconnect.walletconnectv2.engine.EngineInteractor
import org.walletconnect.walletconnectv2.engine.model.EngineData
import org.walletconnect.walletconnectv2.engine.serailising.trySerialize
import org.walletconnect.walletconnectv2.relay.WakuRelayRepository
import org.walletconnect.walletconnectv2.relay.data.jsonrpc.JsonRpcMethod
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
        topic = this.topic.value,
        proposerPublicKey = this.proposer.publicKey,
        ttl = this.ttl.seconds,
        accounts = listOf()
    )

internal fun EngineInteractor.EngineFactory.toRelayInitParams(): WakuRelayRepository.RelayFactory =
    WakuRelayRepository.RelayFactory(useTLs, hostName, apiKey, application)

internal fun EngineData.SessionProposal.toClientSessionProposal(): WalletConnectClientData.SessionProposal =
    WalletConnectClientData.SessionProposal(name, description, url, icons, chains, methods, topic, proposerPublicKey, ttl, accounts)

internal fun WalletConnectClientData.SessionProposal.toEngineSessionProposal(): EngineData.SessionProposal =
    EngineData.SessionProposal(name, description, url, icons, chains, methods, topic, proposerPublicKey, ttl, accounts)
internal fun WalletConnectClientData.SessionProposal.toEngineSessionProposal(accountList: List<String>): EngineData.SessionProposal =
    EngineData.SessionProposal(name, description, url, icons, chains, methods, topic, proposerPublicKey, ttl, accountList)

internal fun EngineData.SettledSession.toClientSettledSession(): WalletConnectClientData.SettledSession =
    WalletConnectClientData.SettledSession(icon, name, uri, topic)

internal fun EngineData.SessionRequest.toClientSessionRequest(): WalletConnectClientData.SessionRequest =
    WalletConnectClientData.SessionRequest(
        topic,
        chainId,
        WalletConnectClientData.SessionRequest.JSONRPCRequest(request.id, request.method, request.params)
    )

internal fun <T> WalletConnectClientData.JsonRpcResponse.JsonRpcResult<T>.toEngineRpcResult(): EngineData.JsonRpcResponse.JsonRpcResult =
    EngineData.JsonRpcResponse.JsonRpcResult(id, result.toString())

internal fun WalletConnectClientData.JsonRpcResponse.JsonRpcError.toEngineRpcError(): EngineData.JsonRpcResponse.JsonRpcError =
    EngineData.JsonRpcResponse.JsonRpcError(id, EngineData.JsonRpcResponse.Error(error.code, error.message))

internal fun WalletConnectClientData.SessionState.toEngineSessionState(): EngineData.SessionState = EngineData.SessionState(accounts)

internal fun <T> WalletConnectClientData.Notification<T>.toEngineNotification(): EngineData.Notification =
    EngineData.Notification(type, data.toString())

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