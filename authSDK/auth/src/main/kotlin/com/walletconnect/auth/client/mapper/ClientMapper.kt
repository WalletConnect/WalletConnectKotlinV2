@file:JvmSynthetic

package com.walletconnect.auth.client.mapper

import com.walletconnect.android_core.common.SDKError
import com.walletconnect.android_core.common.model.ConnectionState
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.common.model.*
import com.walletconnect.auth.signature.SignatureType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@JvmSynthetic
internal fun Auth.Model.AppMetaData.toCommon(): AppMetaData =
    AppMetaData(name, description, url, icons, redirect)

@JvmSynthetic
internal fun String.toCommon(): Issuer = Issuer(this)


@JvmSynthetic
internal fun String.toClient(): Auth.Model.Pairing = Auth.Model.Pairing(this)

@JvmSynthetic
internal fun Auth.Params.Respond.toCommon(): Respond = when (this) {
    is Auth.Params.Respond.Result -> Respond.Result(id, signature)
    is Auth.Params.Respond.Error -> Respond.Error(id, code, message)
}

@JvmSynthetic
internal fun ConnectionState.toClient(): Auth.Event.ConnectionStateChange = Auth.Event.ConnectionStateChange(Auth.Model.ConnectionState(this.isAvailable))

@JvmSynthetic
internal fun SDKError.toClient(): Auth.Event.Error = Auth.Event.Error(Auth.Model.Error(this.exception))

@JvmSynthetic
internal fun Events.OnAuthRequest.toClient(): Auth.Event.AuthRequest = Auth.Event.AuthRequest(id, message)

@JvmSynthetic
internal fun Events.OnAuthResponse.toClient(): Auth.Event.AuthResponse = when (val response = response) {
    is AuthResponse.Error -> Auth.Event.AuthResponse(id, Auth.Model.Response.Error(id, response.code, response.message))
    is AuthResponse.Result -> Auth.Event.AuthResponse(id, Auth.Model.Response.Result(id, response.cacao.toClient()))
}

@JvmSynthetic
internal fun Pairing.toClient(): Auth.Model.Pairing = Auth.Model.Pairing(uri)

@JvmSynthetic
internal fun Auth.Params.Request.toCommon(): PayloadParams = PayloadParams(
    type = SignatureType.EIP191.header,
    chainId = chainId,
    domain = domain,
    aud = aud,
    version = MESSAGE_TEMPLATE_VERSION,
    nonce = nonce,
    iat = DateTimeFormatter.ofPattern(ISO_8601_PATTERN).format(ZonedDateTime.now()),
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
internal fun Cacao.Payload.toClient(): Auth.Model.Cacao.Payload = Auth.Model.Cacao.Payload(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)

@JvmSynthetic
internal fun Cacao.Signature.toClient(): Auth.Model.Cacao.Signature = Auth.Model.Cacao.Signature(t, s, m)

private const val ISO_8601_PATTERN = "uuuu-MM-dd'T'HH:mm:ssXXX"
private const val MESSAGE_TEMPLATE_VERSION = "1"