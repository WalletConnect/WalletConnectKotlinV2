@file:JvmSynthetic

package com.walletconnect.auth.client.mapper

import com.walletconnect.android_core.common.SDKError
import com.walletconnect.android_core.common.model.ConnectionState
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.common.json_rpc.payload.CacaoDTO
import com.walletconnect.auth.common.json_rpc.payload.PayloadParamsDTO
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.auth.signature.SignatureType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@JvmSynthetic
internal fun Auth.Model.AppMetaData.toEngineDO(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons, redirect)

@JvmSynthetic
internal fun String.toEngineDO(): EngineDO.Issuer = EngineDO.Issuer(this)

@JvmSynthetic
internal fun Auth.Params.Respond.toEngineDO(): EngineDO.Respond = when (this) {
    is Auth.Params.Respond.Result -> EngineDO.Respond.Result(id, signature)
    is Auth.Params.Respond.Error -> EngineDO.Respond.Error(id, code, message)
}

@JvmSynthetic
internal fun ConnectionState.toClientEvent(): Auth.Events.ConnectionStateChange = Auth.Events.ConnectionStateChange(Auth.Model.ConnectionState(this.isAvailable))

@JvmSynthetic
internal fun SDKError.toClientEvent(): Auth.Events.Error = Auth.Events.Error(Auth.Model.Error(this.exception))

@JvmSynthetic
internal fun EngineDO.Events.onAuthRequest.toClient(): Auth.Events.AuthRequest = Auth.Events.AuthRequest(id, message)

@JvmSynthetic
internal fun EngineDO.Events.onAuthResponse.toClient(): Auth.Events.AuthResponse = when (val response = response) {
    is EngineDO.AuthResponse.Error -> Auth.Events.AuthResponse(id, Auth.Model.Response.Error(id, response.code, response.message))
    is EngineDO.AuthResponse.Result -> Auth.Events.AuthResponse(id, Auth.Model.Response.Result(id, response.cacao.toClient()))
}

@JvmSynthetic
internal fun EngineDO.Pairing.toClient(): Auth.Model.Pairing = Auth.Model.Pairing(uri)

@JvmSynthetic
internal fun Auth.Params.Request.toEngineDO(): EngineDO.PayloadParams = EngineDO.PayloadParams(
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
internal fun Auth.Model.Cacao.Signature.toEngineDO(): EngineDO.Cacao.Signature = EngineDO.Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Auth.Model.Cacao.Signature.toDTO(): CacaoDTO.SignatureDTO = CacaoDTO.SignatureDTO(t, s, m)

@JvmSynthetic
internal fun EngineDO.Cacao.toClient(): Auth.Model.Cacao = Auth.Model.Cacao(header.toClient(), payload.toClient(), signature.toClient())

@JvmSynthetic
internal fun EngineDO.Cacao.Header.toClient(): Auth.Model.Cacao.Header = Auth.Model.Cacao.Header(t)

@JvmSynthetic
internal fun EngineDO.Cacao.Payload.toClient(): Auth.Model.Cacao.Payload = Auth.Model.Cacao.Payload(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)

@JvmSynthetic
internal fun EngineDO.Cacao.Signature.toClient(): Auth.Model.Cacao.Signature = Auth.Model.Cacao.Signature(t, s, m)

const val ISO_8601_PATTERN = "uuuu-MM-dd'T'HH:mm:ssXXX"
const val MESSAGE_TEMPLATE_VERSION = "1"