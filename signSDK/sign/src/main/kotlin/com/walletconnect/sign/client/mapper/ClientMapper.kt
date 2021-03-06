package com.walletconnect.sign.client.mapper

import android.net.Uri
import android.os.Build
import android.util.Log
import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.walletconnect.sign.core.model.vo.sync.PendingRequestVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.network.data.connection.ConnectionType
import java.net.URI

//TODO: Figure out what to do with models separation
@JvmSynthetic
internal fun EngineDO.ProposedSequence.toClientProposedSequence(): Sign.Model.ProposedSequence =
    when (this) {
        is EngineDO.ProposedSequence.Pairing -> Sign.Model.ProposedSequence.Pairing(this.uri)
        is EngineDO.ProposedSequence.Session -> Sign.Model.ProposedSequence.Session
    }

@JvmSynthetic
internal fun Sign.Model.JsonRpcResponse.toJsonRpcResponseVO(): JsonRpcResponseVO =
    when (this) {
        is Sign.Model.JsonRpcResponse.JsonRpcResult -> this.toRpcResultVO()
        is Sign.Model.JsonRpcResponse.JsonRpcError -> this.toRpcErrorVO()
    }

@JvmSynthetic
internal fun EngineDO.SettledSessionResponse.toClientSettledSessionResponse(): Sign.Model.SettledSessionResponse =
    when (this) {
        is EngineDO.SettledSessionResponse.Result -> Sign.Model.SettledSessionResponse.Result(settledSession.toClientSettledSession())
        is EngineDO.SettledSessionResponse.Error -> Sign.Model.SettledSessionResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun EngineDO.SessionUpdateNamespacesResponse.toClientUpdateSessionNamespacesResponse(): Sign.Model.SessionUpdateResponse =
    when (this) {
        is EngineDO.SessionUpdateNamespacesResponse.Result ->
            Sign.Model.SessionUpdateResponse.Result(topic.value, namespaces.toMapOfClientNamespacesSession())
        is EngineDO.SessionUpdateNamespacesResponse.Error -> Sign.Model.SessionUpdateResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.toClientJsonRpcResponse(): Sign.Model.JsonRpcResponse =
    when (this) {
        is EngineDO.JsonRpcResponse.JsonRpcResult -> this.toClientJsonRpcResult()
        is EngineDO.JsonRpcResponse.JsonRpcError -> this.toClientJsonRpcError()
    }

@JvmSynthetic
internal fun EngineDO.SessionProposal.toClientSessionProposal(): Sign.Model.SessionProposal =
    Sign.Model.SessionProposal(
        name,
        description,
        url,
        icons,
        requiredNamespaces.toMapOfClientNamespacesProposal(),
        proposerPublicKey,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun EngineDO.SessionRequest.toClientSessionRequest(): Sign.Model.SessionRequest =
    Sign.Model.SessionRequest(
        topic = topic,
        chainId = chainId,
        peerMetaData = peerAppMetaData?.toClientAppMetaData(),
        request = Sign.Model.SessionRequest.JSONRPCRequest(
            id = request.id,
            method = request.method,
            params = request.params
        )
    )

@JvmSynthetic
internal fun Sign.Model.JsonRpcResponse.JsonRpcResult.toRpcResultVO(): JsonRpcResponseVO.JsonRpcResult =
    JsonRpcResponseVO.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun Sign.Model.JsonRpcResponse.JsonRpcError.toRpcErrorVO(): JsonRpcResponseVO.JsonRpcError =
    JsonRpcResponseVO.JsonRpcError(id, error = JsonRpcResponseVO.Error(code, message))

@JvmSynthetic
internal fun Sign.Model.SessionEvent.toEngineEvent(chainId: String): EngineDO.Event = EngineDO.Event(name, data, chainId)

@JvmSynthetic
internal fun EngineDO.SessionDelete.toClientDeletedSession(): Sign.Model.DeletedSession =
    Sign.Model.DeletedSession.Success(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionEvent.toClientSessionEvent(): Sign.Model.SessionEvent =
    Sign.Model.SessionEvent(name, data)

@JvmSynthetic
internal fun EngineDO.Session.toClientSettledSession(): Sign.Model.Session =
    Sign.Model.Session(topic.value,
        expiry.seconds,
        namespaces.toMapOfClientNamespacesSession(),
        peerAppMetaData?.toClientAppMetaData())

@JvmSynthetic
internal fun EngineDO.SessionExtend.toClientSettledSession(): Sign.Model.Session =
    Sign.Model.Session(
        topic.value,
        expiry.seconds,
        namespaces.toMapOfClientNamespacesSession(),
        peerAppMetaData?.toClientAppMetaData()
    )

@JvmSynthetic
internal fun EngineDO.SessionRejected.toClientSessionRejected(): Sign.Model.RejectedSession =
    Sign.Model.RejectedSession(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionApproved.toClientSessionApproved(): Sign.Model.ApprovedSession =
    Sign.Model.ApprovedSession(topic, peerAppMetaData?.toClientAppMetaData(), namespaces.toMapOfClientNamespacesSession(), accounts)

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Session>.toMapOfClientNamespacesSession(): Map<String, Sign.Model.Namespace.Session> = this.mapValues { (_, namespace) ->
    Sign.Model.Namespace.Session(namespace.accounts, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
        Sign.Model.Namespace.Session.Extension(extension.accounts, extension.methods, extension.events)
    })
}

@JvmSynthetic
internal fun Sign.Model.AppMetaData.toEngineAppMetaData() = EngineDO.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun EngineDO.AppMetaData.toClientAppMetaData() = Sign.Model.AppMetaData(name, description, url, icons)

@JvmSynthetic
internal fun Sign.Params.Request.toEngineDORequest(): EngineDO.Request =
    EngineDO.Request(sessionTopic, method, params, chainId)

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.JsonRpcResult.toClientJsonRpcResult(): Sign.Model.JsonRpcResponse.JsonRpcResult =
    Sign.Model.JsonRpcResponse.JsonRpcResult(id, result)

@JvmSynthetic
internal fun EngineDO.SessionUpdateNamespaces.toClientSessionsNamespaces(): Sign.Model.UpdatedSession =
    Sign.Model.UpdatedSession(topic.value, namespaces.toMapOfClientNamespacesSession())

@JvmSynthetic
internal fun EngineDO.JsonRpcResponse.JsonRpcError.toClientJsonRpcError(): Sign.Model.JsonRpcResponse.JsonRpcError =
    Sign.Model.JsonRpcResponse.JsonRpcError(id, code = error.code, message = error.message)

@JvmSynthetic
internal fun EngineDO.PairingSettle.toClientSettledPairing(): Sign.Model.Pairing =
    Sign.Model.Pairing(topic.value, metaData?.toClientAppMetaData())

@JvmSynthetic
internal fun List<PendingRequestVO>.mapToPendingRequests(): List<Sign.Model.PendingRequest> = map { request ->
    Sign.Model.PendingRequest(
        request.requestId,
        request.topic,
        request.method,
        request.chainId,
        request.params
    )
}

@JvmSynthetic
internal fun EngineDO.SessionPayloadResponse.toClientSessionPayloadResponse(): Sign.Model.SessionRequestResponse =
    Sign.Model.SessionRequestResponse(topic, chainId, method, result.toClientJsonRpcResponse())

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Proposal>.toMapOfEngineNamespacesProposal(): Map<String, EngineDO.Namespace.Proposal> = mapValues { (_, namespace) ->
    EngineDO.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
        EngineDO.Namespace.Proposal.Extension(extension.chains, extension.methods, extension.events)
    })
}

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Proposal>.toMapOfClientNamespacesProposal(): Map<String, Sign.Model.Namespace.Proposal> = mapValues { (_, namespace) ->
    Sign.Model.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
        Sign.Model.Namespace.Proposal.Extension(extension.chains, extension.methods, extension.events)
    })
}

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Session>.toMapOfEngineNamespacesSession(): Map<String, EngineDO.Namespace.Session> = mapValues { (_, namespace) ->
    EngineDO.Namespace.Session(namespace.accounts, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
        EngineDO.Namespace.Session.Extension(extension.accounts, extension.methods, extension.events)
    })
}

@JvmSynthetic
internal fun List<Sign.Model.RelayProtocolOptions>.toListEngineOfRelayProtocolOptions(): List<EngineDO.RelayProtocolOptions> = map { relayProtocolOptions ->
    EngineDO.RelayProtocolOptions(relayProtocolOptions.protocol, relayProtocolOptions.data)
}

@JvmSynthetic
internal fun Sign.ConnectionType.toRelayConnectionType(): ConnectionType {
    return when (this) {
        Sign.ConnectionType.AUTOMATIC -> ConnectionType.AUTOMATIC
        Sign.ConnectionType.MANUAL -> ConnectionType.MANUAL
    }
}

@JvmSynthetic
internal fun EngineDO.ConnectionState.toClientConnectionState(): Sign.Model.ConnectionState =
    Sign.Model.ConnectionState(isAvailable)

@JvmSynthetic
internal fun EngineDO.InternalError.toClientError(): Sign.Model.Error =
    Sign.Model.Error(this.exception)

@JvmSynthetic
internal fun String.strippedUrl() = Uri.parse(this).run {
    this@run.scheme + "://" + this@run.authority
}

@JvmSynthetic
internal fun String.addUserAgent(): String {
    return Uri.parse(this).buildUpon()
        // TODO: Setup env variable for version and tag. Use env variable here instead of hard coded version
        .appendQueryParameter("ua", """wc-2/kotlin-2.0.0-rc.0/android-${Build.VERSION.RELEASE}""")
        .build()
        .toString()
}