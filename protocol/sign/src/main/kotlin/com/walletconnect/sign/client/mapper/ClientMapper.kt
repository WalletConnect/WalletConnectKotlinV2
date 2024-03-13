@file:JvmSynthetic

package com.walletconnect.sign.client.mapper

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.JsonRpcResponse
import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.android.internal.common.signing.cacao.RECAPS_STATEMENT
import com.walletconnect.android.internal.common.signing.cacao.getStatement
import com.walletconnect.android.utils.toClient
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.common.model.Request
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.engine.model.EngineDO

@JvmSynthetic
internal fun Sign.Model.JsonRpcResponse.toJsonRpcResponse(): JsonRpcResponse =
    when (this) {
        is Sign.Model.JsonRpcResponse.JsonRpcResult -> this.toRpcResult()
        is Sign.Model.JsonRpcResponse.JsonRpcError -> this.toRpcError()
    }

@JvmSynthetic
internal fun EngineDO.SettledSessionResponse.toClientSettledSessionResponse(): Sign.Model.SettledSessionResponse =
    when (this) {
        is EngineDO.SettledSessionResponse.Result -> Sign.Model.SettledSessionResponse.Result(settledSession.toClientActiveSession())
        is EngineDO.SettledSessionResponse.Error -> Sign.Model.SettledSessionResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun EngineDO.SessionAuthenticateResponse.toClientSessionAuthenticateResponse(): Sign.Model.SessionAuthenticateResponse =
    when (this) {
        is EngineDO.SessionAuthenticateResponse.Result -> Sign.Model.SessionAuthenticateResponse.Result(id, cacaos.toClient(), session?.toClientActiveSession())
        is EngineDO.SessionAuthenticateResponse.Error -> Sign.Model.SessionAuthenticateResponse.Error(id, code, message)
    }

@JvmSynthetic
internal fun EngineDO.SessionUpdateNamespacesResponse.toClientUpdateSessionNamespacesResponse(): Sign.Model.SessionUpdateResponse =
    when (this) {
        is EngineDO.SessionUpdateNamespacesResponse.Result -> Sign.Model.SessionUpdateResponse.Result(topic.value, namespaces.toMapOfClientNamespacesSession())
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
        pairingTopic,
        name,
        description,
        url,
        icons,
        redirect,
        requiredNamespaces.toMapOfClientNamespacesProposal(),
        optionalNamespaces.toMapOfClientNamespacesProposal(),
        properties,
        proposerPublicKey,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun EngineDO.VerifyContext.toCore(): Sign.Model.VerifyContext =
    Sign.Model.VerifyContext(id, origin, this.validation.toClientValidation(), verifyUrl, isScam)

internal fun Validation.toClientValidation(): Sign.Model.Validation =
    when (this) {
        Validation.VALID -> Sign.Model.Validation.VALID
        Validation.INVALID -> Sign.Model.Validation.INVALID
        Validation.UNKNOWN -> Sign.Model.Validation.UNKNOWN
    }

@JvmSynthetic
internal fun EngineDO.SessionRequest.toClientSessionRequest(): Sign.Model.SessionRequest =
    Sign.Model.SessionRequest(
        topic = topic,
        chainId = chainId,
        peerMetaData = peerAppMetaData?.toClient(),
        request = Sign.Model.SessionRequest.JSONRPCRequest(
            id = request.id,
            method = request.method,
            params = request.params
        )
    )

@JvmSynthetic
internal fun Sign.Model.PayloadParams.toEngine(): EngineDO.PayloadParams = with(this) {
    EngineDO.PayloadParams(
        type = CacaoType.CAIP222.header,
        chains = chains,
        domain = domain,
        aud = aud,
        nonce = nonce,
        nbf = nbf,
        exp = exp,
        statement = statement,
        requestId = requestId,
        resources = resources,
        iat = iat,
        version = "1"
    )
}

@JvmSynthetic
internal fun Sign.Params.Authenticate.toAuthenticate(): EngineDO.Authenticate = with(this) {
    EngineDO.Authenticate(
        type = CacaoType.EIP4361.header,
        chains = chains,
        domain = domain,
        aud = uri,
        nonce = nonce,
        nbf = nbf,
        exp = exp,
        statement = statement,
        requestId = requestId,
        resources = resources,
        methods = methods,
        expiry = expiry
    )
}

@JvmSynthetic
internal fun Sign.Model.PayloadParams.toCacaoPayload(issuer: String): Sign.Model.Cacao.Payload = with(this) {
    Sign.Model.Cacao.Payload(issuer, domain, aud, "1", nonce, iat = iat, nbf, exp, getStatement(), requestId, resources)
}

private fun Sign.Model.PayloadParams.getStatement() =
    if (statement?.contains(RECAPS_STATEMENT) == true) {
        statement
    } else {
        Pair(statement, resources).getStatement()
    }

@JvmSynthetic
internal fun List<Sign.Model.Cacao>.toCommon(): List<Cacao> = this.map {
    with(it) {
        Cacao(
            Cacao.Header(header.t),
            Cacao.Payload(
                payload.iss,
                payload.domain,
                payload.aud,
                payload.version,
                payload.nonce,
                payload.iat,
                payload.nbf,
                payload.exp,
                payload.statement,
                payload.requestId,
                payload.resources
            ),
            Cacao.Signature(signature.t, signature.s, signature.m)
        )
    }
}

@JvmSynthetic
internal fun List<Cacao>.toClient(): List<Sign.Model.Cacao> = this.map {
    with(it) {
        Sign.Model.Cacao(
            Sign.Model.Cacao.Header(header.t),
            Sign.Model.Cacao.Payload(
                payload.iss,
                payload.domain,
                payload.aud,
                payload.version,
                payload.nonce,
                payload.iat,
                payload.nbf,
                payload.exp,
                payload.statement,
                payload.requestId,
                payload.resources
            ),
            Sign.Model.Cacao.Signature(signature.t, signature.s, signature.m)
        )
    }
}

@JvmSynthetic
internal fun Sign.Model.JsonRpcResponse.JsonRpcResult.toRpcResult(): JsonRpcResponse.JsonRpcResult = JsonRpcResponse.JsonRpcResult(id, result = result)

@JvmSynthetic
internal fun Sign.Model.JsonRpcResponse.JsonRpcError.toRpcError(): JsonRpcResponse.JsonRpcError = JsonRpcResponse.JsonRpcError(id, error = JsonRpcResponse.Error(code, message))

@JvmSynthetic
internal fun Sign.Model.SessionEvent.toEngineEvent(chainId: String): EngineDO.Event = EngineDO.Event(name, data, chainId)

@JvmSynthetic
internal fun EngineDO.SessionDelete.toClientDeletedSession(): Sign.Model.DeletedSession =
    Sign.Model.DeletedSession.Success(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionEvent.toClientSessionEvent(): Sign.Model.SessionEvent =
    Sign.Model.SessionEvent(name, data)

@JvmSynthetic
internal fun EngineDO.SessionAuthenticateEvent.toClientSessionAuthenticate(): Sign.Model.SessionAuthenticate = with(payloadParams) {
    Sign.Model.SessionAuthenticate(
        id,
        pairingTopic,
        participant.toClient(),
        payloadParams.toClient(),
        expiryTimestamp
    )
}

@JvmSynthetic
internal fun EngineDO.Participant.toClient(): Sign.Model.SessionAuthenticate.Participant = Sign.Model.SessionAuthenticate.Participant(publicKey, metadata.toClient())

@JvmSynthetic
internal fun EngineDO.PayloadParams.toClient(): Sign.Model.PayloadParams = Sign.Model.PayloadParams(
    type = type,
    chains = chains,
    domain = domain,
    aud = aud,
    nonce = nonce,
    nbf = nbf,
    exp = exp,
    statement = statement,
    requestId = requestId,
    resources = resources,
    iat = iat
)

@JvmSynthetic
internal fun EngineDO.Session.toClientActiveSession(): Sign.Model.Session =
    Sign.Model.Session(
        pairingTopic,
        topic.value,
        expiry.seconds,
        requiredNamespaces.toMapOfClientNamespacesProposal(),
        optionalNamespaces?.toMapOfClientNamespacesProposal(),
        namespaces.toMapOfClientNamespacesSession(),
        peerAppMetaData?.toClient()
    )

@JvmSynthetic
internal fun EngineDO.SessionExtend.toClientActiveSession(): Sign.Model.Session =
    Sign.Model.Session(
        pairingTopic,
        topic.value,
        expiry.seconds,
        requiredNamespaces.toMapOfClientNamespacesProposal(),
        optionalNamespaces?.toMapOfClientNamespacesProposal(),
        namespaces.toMapOfClientNamespacesSession(),
        peerAppMetaData?.toClient()
    )

@JvmSynthetic
internal fun EngineDO.SessionRejected.toClientSessionRejected(): Sign.Model.RejectedSession =
    Sign.Model.RejectedSession(topic, reason)

@JvmSynthetic
internal fun EngineDO.SessionApproved.toClientSessionApproved(): Sign.Model.ApprovedSession =
    Sign.Model.ApprovedSession(topic, peerAppMetaData?.toClient(), namespaces.toMapOfClientNamespacesSession(), accounts)

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Session>.toMapOfClientNamespacesSession(): Map<String, Sign.Model.Namespace.Session> =
    this.mapValues { (_, namespace) ->
        Sign.Model.Namespace.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Sign.Params.Request.toEngineDORequest(): EngineDO.Request =
    EngineDO.Request(sessionTopic, method, params, chainId, expiry?.let { Expiry(it) })

@JvmSynthetic
internal fun Sign.Params.Request.toSentRequest(requestId: Long): Sign.Model.SentRequest =
    Sign.Model.SentRequest(requestId, sessionTopic, method, params, chainId)

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
    Sign.Model.Pairing(topic.value, appMetaData?.toClient())

@JvmSynthetic
internal fun List<Request<String>>.mapToPendingRequests(): List<Sign.Model.PendingRequest> = map { request ->
    Sign.Model.PendingRequest(
        request.id,
        request.topic.value,
        request.method,
        request.chainId,
        request.params
    )
}

@JvmSynthetic
internal fun List<EngineDO.SessionRequest>.mapToPendingSessionRequests(): List<Sign.Model.SessionRequest> =
    map { request -> request.toClientSessionRequest() }

@JvmSynthetic
internal fun Request<SignParams.SessionAuthenticateParams>.toClient(): Sign.Model.SessionAuthenticate =
    Sign.Model.SessionAuthenticate(
        id = id,
        topic = topic.value,
        participant = Sign.Model.SessionAuthenticate.Participant(params.requester.publicKey, params.requester.metadata.toClient()),
        payloadParams = params.toClient(),
        expiry = params.expiryTimestamp
    )

@JvmSynthetic
internal fun SignParams.SessionAuthenticateParams.toClient(): Sign.Model.PayloadParams = with(this.authPayload) {
    Sign.Model.PayloadParams(
        type = type,
        chains = chains,
        domain = domain,
        aud = aud,
        nonce = nonce,
        nbf = nbf,
        exp = exp,
        statement = statement,
        requestId = requestId,
        resources = resources,
        iat = iat
    )
}

@JvmSynthetic
internal fun EngineDO.SessionPayloadResponse.toClientSessionPayloadResponse(): Sign.Model.SessionRequestResponse =
    Sign.Model.SessionRequestResponse(topic, chainId, method, result.toClientJsonRpcResponse())

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Proposal>.toMapOfEngineNamespacesRequired(): Map<String, EngineDO.Namespace.Proposal> =
    mapValues { (_, namespace) ->
        EngineDO.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Proposal>.toMapOfEngineNamespacesOptional(): Map<String, EngineDO.Namespace.Proposal> =
    mapValues { (_, namespace) ->
        EngineDO.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, EngineDO.Namespace.Proposal>.toMapOfClientNamespacesProposal(): Map<String, Sign.Model.Namespace.Proposal> =
    mapValues { (_, namespace) ->
        Sign.Model.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Session>.toMapOfEngineNamespacesSession(): Map<String, EngineDO.Namespace.Session> =
    mapValues { (_, namespace) ->
        EngineDO.Namespace.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Proposal>.toProposalNamespacesVO(): Map<String, Namespace.Proposal> =
    mapValues { (_, namespace) ->
        Namespace.Proposal(chains = namespace.chains, methods = namespace.methods, events = namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Session>.toSessionNamespacesVO(): Map<String, Namespace.Session> =
    mapValues { (_, namespace) ->
        Namespace.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Namespace.Session>.toCore(): Map<String, Sign.Model.Namespace.Session> =
    mapValues { (_, namespace) ->
        Sign.Model.Namespace.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Core.Model.Namespace.Proposal>.toSign(): Map<String, Sign.Model.Namespace.Proposal> =
    mapValues { (_, namespace) ->
        Sign.Model.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun ConnectionState.toClientConnectionState(): Sign.Model.ConnectionState =
    Sign.Model.ConnectionState(isAvailable)

@JvmSynthetic
internal fun EngineDO.ExpiredProposal.toClient(): Sign.Model.ExpiredProposal =
    Sign.Model.ExpiredProposal(pairingTopic, proposerPublicKey)

@JvmSynthetic
internal fun EngineDO.ExpiredRequest.toClient(): Sign.Model.ExpiredRequest =
    Sign.Model.ExpiredRequest(topic, id)

@JvmSynthetic
internal fun SDKError.toClientError(): Sign.Model.Error =
    Sign.Model.Error(this.exception)

@JvmSynthetic
internal fun Core.Model.Message.SessionProposal.toSign(): Sign.Model.Message.SessionProposal =
    Sign.Model.Message.SessionProposal(
        id,
        pairingTopic,
        name,
        description,
        url,
        icons,
        redirect,
        requiredNamespaces.toSign(),
        optionalNamespaces.toSign(),
        properties,
        proposerPublicKey,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun Core.Model.Message.SessionAuthenticate.toSign(): Sign.Model.Message.SessionAuthenticate =
    Sign.Model.Message.SessionAuthenticate(
        id, topic, metadata, payloadParams.toClient(), expiry
    )

private fun Core.Model.Message.SessionAuthenticate.PayloadParams.toClient(): Sign.Model.PayloadParams {
    return with(this) {
        Sign.Model.PayloadParams(
            chains = chains,
            domain = domain,
            nonce = nonce,
            aud = aud,
            type = type,
            nbf = nbf,
            exp = exp,
            iat = iat,
            statement = requestId,
            resources = resources,
            requestId = requestId,
        )
    }
}

@JvmSynthetic
internal fun Core.Model.Message.SessionRequest.toSign(): Sign.Model.Message.SessionRequest =
    Sign.Model.Message.SessionRequest(
        topic,
        chainId,
        peerMetaData,
        Sign.Model.Message.SessionRequest.JSONRPCRequest(request.id, request.method, request.params)
    )