@file:JvmSynthetic

package com.walletconnect.auth.client.mapper

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.common.model.*
import java.text.SimpleDateFormat
import java.util.*

@JvmSynthetic
internal fun Auth.Params.Respond.toCommon(): Respond = when (this) {
    is Auth.Params.Respond.Result -> Respond.Result(id, signature, issuer)
    is Auth.Params.Respond.Error -> Respond.Error(id, code, message)
}

@JvmSynthetic
internal fun ConnectionState.toClient(): Auth.Event.ConnectionStateChange =
    Auth.Event.ConnectionStateChange(Auth.Model.ConnectionState(this.isAvailable))

@JvmSynthetic
internal fun SDKError.toClient(): Auth.Event.Error = Auth.Event.Error(Auth.Model.Error(this.exception))

@JvmSynthetic
internal fun Events.OnAuthRequest.toClientAuthRequest(): Auth.Event.AuthRequest = Auth.Event.AuthRequest(id, pairingTopic, payloadParams.toClient())

@JvmSynthetic
internal fun Events.OnAuthRequest.toClientAuthContext(): Auth.Event.VerifyContext = Auth.Event.VerifyContext(id, authContext.origin, authContext.validation.toClientValidation(), authContext.verifyUrl)

@JvmSynthetic
internal fun Validation.toClientValidation(): Auth.Model.Validation =
    when (this) {
        Validation.VALID -> Auth.Model.Validation.VALID
        Validation.INVALID -> Auth.Model.Validation.INVALID
        Validation.UNKNOWN -> Auth.Model.Validation.UNKNOWN
    }

internal fun PayloadParams.toClient(): Auth.Model.PayloadParams =
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
internal fun Events.OnAuthResponse.toClient(): Auth.Event.AuthResponse = when (val response = response) {
    is AuthResponse.Error -> Auth.Event.AuthResponse(Auth.Model.Response.Error(id, response.code, response.message))
    is AuthResponse.Result -> Auth.Event.AuthResponse(Auth.Model.Response.Result(id, response.cacao.toClient()))
}

@JvmSynthetic
internal fun Auth.Params.Request.toCommon(): PayloadParams = PayloadParams(
    type = CacaoType.EIP4361.header,
    chainId = chainId,
    domain = domain,
    aud = aud,
    version = Cacao.Payload.CURRENT_VERSION,
    nonce = nonce,
    iat = SimpleDateFormat(Cacao.Payload.ISO_8601_PATTERN).format(Calendar.getInstance().time),
    nbf = nbf,
    exp = exp,
    statement = statement,
    requestId = requestId,
    resources = resources,
)

@JvmSynthetic
internal fun List<PendingRequest>.toClient(): List<Auth.Model.PendingRequest> =
    map { request ->
        Auth.Model.PendingRequest(
            request.id,
            request.pairingTopic,
            request.payloadParams.toClient()
        )
    }

@JvmSynthetic
internal fun Auth.Model.PayloadParams.toCommon(): PayloadParams =
    PayloadParams(
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
internal fun Auth.Model.Cacao.Signature.toCommon(): Cacao.Signature = Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Cacao.toClient(): Auth.Model.Cacao = Auth.Model.Cacao(header.toClient(), payload.toClient(), signature.toClient())

@JvmSynthetic
internal fun Cacao.Header.toClient(): Auth.Model.Cacao.Header = Auth.Model.Cacao.Header(t)

@JvmSynthetic
internal fun Cacao.Payload.toClient(): Auth.Model.Cacao.Payload =
    Auth.Model.Cacao.Payload(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)

@JvmSynthetic
internal fun Cacao.Signature.toClient(): Auth.Model.Cacao.Signature = Auth.Model.Cacao.Signature(t, s, m)