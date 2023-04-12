package com.walletconnect.web3.wallet.client

import com.walletconnect.auth.client.Auth
import com.walletconnect.sign.client.Sign

@JvmSynthetic
internal fun Map<String, Wallet.Model.Namespace.Session>.toSign(): Map<String, Sign.Model.Namespace.Session> =
    mapValues { (_, namespace) ->
        Sign.Model.Namespace.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Session>.toWallet(): Map<String, Wallet.Model.Namespace.Session> =
    mapValues { (_, namespace) ->
        Wallet.Model.Namespace.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Proposal>.toWalletProposalNamespaces(): Map<String, Wallet.Model.Namespace.Proposal> =
    mapValues { (_, namespace) ->
        Wallet.Model.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Wallet.Model.JsonRpcResponse.toSign(): Sign.Model.JsonRpcResponse =
    when (this) {
        is Wallet.Model.JsonRpcResponse.JsonRpcResult -> this.toSign()
        is Wallet.Model.JsonRpcResponse.JsonRpcError -> this.toSign()
    }

@JvmSynthetic
internal fun Wallet.Model.JsonRpcResponse.JsonRpcResult.toSign(): Sign.Model.JsonRpcResponse.JsonRpcResult =
    Sign.Model.JsonRpcResponse.JsonRpcResult(id, result)

@JvmSynthetic
internal fun Wallet.Model.JsonRpcResponse.JsonRpcError.toSign(): Sign.Model.JsonRpcResponse.JsonRpcError =
    Sign.Model.JsonRpcResponse.JsonRpcError(id, code, message)

@JvmSynthetic
internal fun Wallet.Params.AuthRequestResponse.toAuth(): Auth.Params.Respond = when (this) {
    is Wallet.Params.AuthRequestResponse.Result -> Auth.Params.Respond.Result(id, signature.toAuth(), issuer)
    is Wallet.Params.AuthRequestResponse.Error -> Auth.Params.Respond.Error(id, code, message)
}

@JvmSynthetic
internal fun Wallet.Model.Cacao.Signature.toAuth(): Auth.Model.Cacao.Signature = Auth.Model.Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Wallet.Model.SessionEvent.toSign(): Sign.Model.SessionEvent = Sign.Model.SessionEvent(name, data)

@JvmSynthetic
internal fun Wallet.Model.PayloadParams.toSign(): Auth.Model.PayloadParams =
    Auth.Model.PayloadParams(
        type = type,
        chainId = chainId,
        domain = domain,
        aud = aud,
        version = version,
        nonce = nonce,
        iat = iat,
        nbf = nbf,
        exp = exp,
        statement = statement,
        requestId = requestId,
        resources = resources,
    )

@JvmSynthetic
internal fun Sign.Model.Session.toWallet(): Wallet.Model.Session = Wallet.Model.Session(pairingTopic, topic, expiry, namespaces.toWallet(), metaData)

@JvmSynthetic
internal fun List<Sign.Model.PendingRequest>.mapToPendingRequests(): List<Wallet.Model.PendingSessionRequest> = map { request ->
    Wallet.Model.PendingSessionRequest(
        request.requestId,
        request.topic,
        request.method,
        request.chainId,
        request.params
    )
}

@JvmSynthetic
internal fun List<Sign.Model.SessionRequest>.mapToPendingSessionRequests(): List<Wallet.Model.SessionRequest> = map { request ->
    Wallet.Model.SessionRequest(
        request.topic,
        request.chainId,
        request.peerMetaData,
        Wallet.Model.SessionRequest.JSONRPCRequest(request.request.id, request.request.method, request.request.params)
    )
}

internal fun Auth.Model.PayloadParams.toWallet(): Wallet.Model.PayloadParams =
    Wallet.Model.PayloadParams(
        type = type,
        chainId = chainId,
        domain = domain,
        aud = aud,
        version = version,
        nonce = nonce,
        iat = iat,
        nbf = nbf,
        exp = exp,
        statement = statement,
        requestId = requestId,
        resources = resources,
    )

@JvmSynthetic
internal fun List<Auth.Model.PendingRequest>.toWallet(): List<Wallet.Model.PendingAuthRequest> =
    map { request ->
        Wallet.Model.PendingAuthRequest(
            request.id,
            request.pairingTopic,
            request.payloadParams.toWallet()
        )
    }

@JvmSynthetic
internal fun Sign.Model.SessionProposal.toWallet(): Wallet.Model.SessionProposal =
    Wallet.Model.SessionProposal(
        pairingTopic,
        name,
        description,
        url,
        icons,
        requiredNamespaces.toWalletProposalNamespaces(),
        optionalNamespaces.toWalletProposalNamespaces(),
        properties,
        proposerPublicKey,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun Sign.Model.SessionRequest.toWallet(): Wallet.Model.SessionRequest =
    Wallet.Model.SessionRequest(
        topic = topic,
        chainId = chainId,
        peerMetaData = peerMetaData,
        request = Wallet.Model.SessionRequest.JSONRPCRequest(
            id = request.id,
            method = request.method,
            params = request.params
        )
    )

@JvmSynthetic
internal fun Sign.Model.DeletedSession.toWallet(): Wallet.Model.SessionDelete =
    when (this) {
        is Sign.Model.DeletedSession.Success -> Wallet.Model.SessionDelete.Success(topic, reason)
        is Sign.Model.DeletedSession.Error -> Wallet.Model.SessionDelete.Error(error)
    }

@JvmSynthetic
internal fun Sign.Model.SettledSessionResponse.toWallet(): Wallet.Model.SettledSessionResponse =
    when (this) {
        is Sign.Model.SettledSessionResponse.Result -> Wallet.Model.SettledSessionResponse.Result(session.toWallet())
        is Sign.Model.SettledSessionResponse.Error -> Wallet.Model.SettledSessionResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun Sign.Model.SessionUpdateResponse.toWallet(): Wallet.Model.SessionUpdateResponse =
    when (this) {
        is Sign.Model.SessionUpdateResponse.Result -> Wallet.Model.SessionUpdateResponse.Result(topic, namespaces.toWallet())
        is Sign.Model.SessionUpdateResponse.Error -> Wallet.Model.SessionUpdateResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun Auth.Event.AuthRequest.toWallet(): Wallet.Model.AuthRequest = Wallet.Model.AuthRequest(id, pairingTopic, payloadParams.toWallet())

@JvmSynthetic
internal fun Auth.Model.Cacao.Signature.toWallet(): Wallet.Model.Cacao.Signature =
    Wallet.Model.Cacao.Signature(t, s, m)