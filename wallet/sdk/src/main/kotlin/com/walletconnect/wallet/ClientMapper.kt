package com.walletconnect.wallet

import com.walletconnect.auth.client.Auth
import com.walletconnect.sign.client.Sign

@JvmSynthetic
internal fun Map<String, Wallet.Model.Namespace.Session>.toSign(): Map<String, Sign.Model.Namespace.Session> =
    mapValues { (_, namespace) ->
        Sign.Model.Namespace.Session(namespace.accounts, namespace.methods, namespace.events, namespace.extensions?.map { extension ->
            Sign.Model.Namespace.Session.Extension(extension.accounts, extension.methods, extension.events)
        })
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
