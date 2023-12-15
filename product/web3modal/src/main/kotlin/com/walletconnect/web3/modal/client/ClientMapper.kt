package com.walletconnect.web3.modal.client

import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.sign.client.Sign

// toModal()

internal fun Sign.Model.ApprovedSession.toModal() = Modal.Model.ApprovedSession(topic, metaData, namespaces.toModal(), accounts)

internal fun Map<String, Sign.Model.Namespace.Session>.toModal() = mapValues { (_, namespace) -> Modal.Model.Namespace.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)  }

internal fun Sign.Model.RejectedSession.toModal() = Modal.Model.RejectedSession(topic, reason)

internal fun Sign.Model.UpdatedSession.toModal() = Modal.Model.UpdatedSession(topic, namespaces.toModal())

internal fun Sign.Model.SessionEvent.toModal() = Modal.Model.SessionEvent(name, data)

internal fun Sign.Model.Session.toModal() = Modal.Model.Session(pairingTopic, topic, expiry, namespaces.toModal(), metaData)

internal fun Sign.Model.DeletedSession.toModal() = when(this) {
    is Sign.Model.DeletedSession.Error -> Modal.Model.DeletedSession.Error(error)
    is Sign.Model.DeletedSession.Success -> Modal.Model.DeletedSession.Success(topic, reason)
}

internal fun Sign.Model.SessionRequestResponse.toModal() = Modal.Model.SessionRequestResponse(topic, chainId, method, result.toModal())

@JvmSynthetic
internal fun Sign.Model.SessionAuthenticateResponse.toModal(): Modal.Model.SessionAuthenticateResponse =
    when (this) {
        is Sign.Model.SessionAuthenticateResponse.Result -> Modal.Model.SessionAuthenticateResponse.Result(id, cacaos.toClient())
        is Sign.Model.SessionAuthenticateResponse.Error -> Modal.Model.SessionAuthenticateResponse.Error(id, code, message)
    }

internal fun Sign.Model.JsonRpcResponse.toModal() = when(this) {
    is Sign.Model.JsonRpcResponse.JsonRpcError -> Modal.Model.JsonRpcResponse.JsonRpcError(id, code, message)
    is Sign.Model.JsonRpcResponse.JsonRpcResult -> Modal.Model.JsonRpcResponse.JsonRpcResult(id, result)
}

@JvmSynthetic
internal fun List<Sign.Model.Cacao>.toClient(): List<Modal.Model.Cacao> = mutableListOf<Modal.Model.Cacao>().apply {
    this@toClient.forEach { cacao: Sign.Model.Cacao ->
        with(cacao) {
            add(
                Modal.Model.Cacao(
                    Modal.Model.Cacao.Header(header.t),
                    Modal.Model.Cacao.Payload(
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
                    Modal.Model.Cacao.Signature(signature.t, signature.s, signature.m)
                )
            )
        }
    }
}

internal fun Sign.Model.ConnectionState.toModal() = Modal.Model.ConnectionState(isAvailable)

internal fun Sign.Model.Error.toModal() = Modal.Model.Error(throwable)

internal fun Sign.Params.Disconnect.toModal() = Modal.Params.Disconnect(sessionTopic)

internal fun Sign.Model.SentRequest.toModal() = Modal.Model.SentRequest(requestId, sessionTopic, method, params, chainId)

internal fun Sign.Model.Ping.Success.toModal() = Modal.Model.Ping.Success(topic)

internal fun Sign.Model.Ping.Error.toModal() = Modal.Model.Ping.Error(error)

// toSign()
internal fun Modal.Params.Connect.toSign() = Sign.Params.Connect(namespaces?.toSign(), optionalNamespaces?.toSign(), properties, pairing)

internal fun Modal.Params.Authenticate.toSign(): Sign.Params.Authenticate = with(this) {
    Sign.Params.Authenticate(pairingTopic, payloadParams.toSign())
}

internal fun Modal.Model.PayloadParams.toSign(): Sign.Model.PayloadParams = with(this) {
    Sign.Model.PayloadParams(CacaoType.CAIP222.header, chains, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)
}

internal fun Map<String, Modal.Model.Namespace.Proposal>.toSign() = mapValues { (_, namespace) -> Sign.Model.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events) }

internal fun Modal.Params.Disconnect.toSign() = Sign.Params.Disconnect(sessionTopic)

internal fun Modal.Params.Ping.toSign() = Sign.Params.Ping(topic)

internal fun Modal.Params.Request.toSign(sessionTopic: String, chainId: String) = Sign.Params.Request(sessionTopic, method, params, chainId, expiry)


internal fun Modal.Listeners.SessionPing.toSign() = object : Sign.Listeners.SessionPing {
    override fun onSuccess(pingSuccess: Sign.Model.Ping.Success) {
        this@toSign.onSuccess(pingSuccess.toModal())
    }

    override fun onError(pingError: Sign.Model.Ping.Error) {
        this@toSign.onError(pingError.toModal())
    }

}
