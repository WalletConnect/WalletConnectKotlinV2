package com.walletconnect.walletconnectv2.engine.model.mapper

import com.walletconnect.walletconnectv2.common.model.vo.*
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.PairingParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.PreSettlementPairingVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.proposal.*
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingParticipantVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.pairing.before.success.PairingStateVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.SessionParamsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.after.params.SessionPermissionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.AppMetaDataVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.RelayProtocolOptionsVO
import com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.before.proposal.SessionProposedPermissionsVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.PairingVO
import com.walletconnect.walletconnectv2.common.model.vo.sequence.SessionVO
import com.walletconnect.walletconnectv2.engine.model.EngineDO
import com.walletconnect.walletconnectv2.relay.model.utils.JsonRpcMethod
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStatus
import com.walletconnect.walletconnectv2.util.Empty
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder
import kotlin.time.Duration

internal fun String.toPairProposal(): PairingParamsVO.Proposal {
    val properUriString = if (contains("wc://")) this else replace("wc:", "wc://")
    val pairUri = URI(properUriString)
    val mapOfQueryParameters: Map<String, String> =
        pairUri.query.split("&")
            .associate { query -> query.substringBefore("=") to query.substringAfter("=") }
    val relay = JSONObject(mapOfQueryParameters["relay"] ?: "{}")
    val publicKey = mapOfQueryParameters["publicKey"] ?: ""
    val controller: Boolean = mapOfQueryParameters["controller"].toBoolean()
    val ttl: Long = Duration.days(30).inWholeSeconds

    return PairingParamsVO.Proposal(
        pendingTopic = TopicVO(pairUri.userInfo),
        relay = relay,
        pairingProposer = PairingProposerVO(publicKey, controller),
        pairingSignal = PairingSignalVO("uri", PairingSignalParamsVO(properUriString)),
        permissions = PairingProposedPermissionsVO(JsonRPCVO(listOf(JsonRpcMethod.WC_SESSION_PROPOSE))),
        ttl = TtlVO(ttl)
    )
}

internal fun EngineDO.WalletConnectUri.toAbsoluteString(): String =
    "wc:$topic@$version?controller=$isController&publicKey=$publicKey&relay=${relay.toUrlEncodedString()}"

internal fun RelayProtocolOptionsVO.toUrlEncodedString(): String = URLEncoder.encode(JSONObject().put("protocol", protocol).toString(), "UTF-8")

internal fun PairingParamsVO.Proposal.toPairingSuccess(
    settleTopic: TopicVO,
    expiry: ExpiryVO,
    selfPublicKey: PublicKey
): PairingParamsVO.ApproveParams =
    PairingParamsVO.ApproveParams(
        settledTopic = settleTopic,
        relay = relay,
        responder = PairingParticipantVO(publicKey = selfPublicKey.keyAsHex),
        expiry = expiry,
        state = PairingStateVO(null)
    )

internal fun PairingParamsVO.Proposal.toApprove(
    id: Long,
    settleTopic: TopicVO,
    expiry: ExpiryVO,
    selfPublicKey: PublicKey
): PreSettlementPairingVO.Approve = PreSettlementPairingVO.Approve(id = id, params = this.toPairingSuccess(settleTopic, expiry, selfPublicKey))

internal fun EngineDO.AppMetaData.toClientSyncMetaData() =
    AppMetaDataVO(name, description, url, icons)

internal fun EngineDO.SessionPermissions.toSessionsPermissions(): SessionPermissionsVO =
    SessionPermissionsVO(
        blockchain?.chains?.let { chains -> SessionProposedPermissionsVO.Blockchain(chains) },
        jsonRpc?.methods?.let { methods -> SessionProposedPermissionsVO.JsonRpc(methods) }
    )

internal fun EngineDO.SessionPermissions.toSessionsProposedPermissions(): SessionProposedPermissionsVO =
    SessionProposedPermissionsVO(
        blockchain!!.chains.let { chains -> SessionProposedPermissionsVO.Blockchain(chains) },
        jsonRpc!!.methods.let { methods -> SessionProposedPermissionsVO.JsonRpc(methods) }
    )

internal fun EngineDO.JsonRpcResponse.JsonRpcResult.toJsonRpcResponseVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result)

internal fun PairingParamsVO.PayloadParams.toEngineDOSessionProposal(): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name = this.request.params.proposer.metadata?.name!!,
        description = this.request.params.proposer.metadata.description,
        url = this.request.params.proposer.metadata.url,
        icons = this.request.params.proposer.metadata.icons.map { URI(it) },
        chains = this.request.params.permissions.blockchain.chains,
        methods = this.request.params.permissions.jsonRpc.methods,
        types = this.request.params.permissions.notifications.types,
        topic = this.request.params.topic.value,
        proposerPublicKey = this.request.params.proposer.publicKey,
        isController = this.request.params.proposer.controller,
        ttl = this.request.params.ttl.seconds,
        accounts = listOf()
    )

internal fun SessionParamsVO.SessionPayloadParams.toEngineDOSessionRequest(topic: TopicVO, requestId: Long): EngineDO.SessionRequest =
    EngineDO.SessionRequest(
        topic.value,
        chainId,
        EngineDO.SessionRequest.JSONRPCRequest(requestId, this.request.method, this.request.params.toString())
    )

internal fun SessionParamsVO.DeleteParams.toEngineDoDeleteSession(topic: TopicVO): EngineDO.DeletedSession =
    EngineDO.DeletedSession(topic.value, reason.message)

internal fun SessionParamsVO.NotificationParams.toEngineDoSessionNotification(topic: TopicVO): EngineDO.SessionNotification =
    EngineDO.SessionNotification(topic.value, type, data.toString())

internal fun EngineDO.SessionProposal.toAcknowledgedSession(topic: TopicVO, expiry: ExpiryVO): EngineDO.SettledSession =
    EngineDO.SettledSession(
        topic,
        expiry,
        SequenceStatus.ACKNOWLEDGED,
        accounts,
        EngineDO.AppMetaData(name, description, url, icons.map { iconUri -> iconUri.toString() }),
        EngineDO.SettledSession.Permissions(
            EngineDO.SettledSession.Permissions.Blockchain(chains),
            EngineDO.SettledSession.Permissions.JsonRpc(methods),
            EngineDO.SettledSession.Permissions.Notifications(types)
        )
    )

internal fun SessionVO.toEngineDOSessionProposal(peerPublicKey: PublicKey): EngineDO.SessionProposal =
    EngineDO.SessionProposal(
        name = appMetaData?.name ?: String.Empty,
        description = appMetaData?.description ?: String.Empty,
        url = appMetaData?.url ?: String.Empty,
        icons = appMetaData?.icons?.map { URI(it) } ?: emptyList(),
        chains = chains,
        methods = methods,
        types = types,
        topic = topic.value,
        proposerPublicKey = peerPublicKey.keyAsHex,
        ttl = ttl.seconds,
        accounts = accounts
    )

internal fun SessionVO.toEngineDOSettledSession(): EngineDO.SettledSession =
    EngineDO.SettledSession(
        topic, expiry, status,
        accounts, appMetaData?.toEngineDOAppMetaData(),
        EngineDO.SettledSession.Permissions(
            EngineDO.SettledSession.Permissions.Blockchain(chains),
            EngineDO.SettledSession.Permissions.JsonRpc(methods),
            EngineDO.SettledSession.Permissions.Notifications(types)
        )
    )

private fun AppMetaDataVO.toEngineDOAppMetaData(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons)

internal fun PairingVO.toEngineDOSettledPairing(sessionPermissions: EngineDO.SessionPermissions): EngineDO.SettledPairing =
    EngineDO.SettledPairing(topic, relay, sessionPermissions)