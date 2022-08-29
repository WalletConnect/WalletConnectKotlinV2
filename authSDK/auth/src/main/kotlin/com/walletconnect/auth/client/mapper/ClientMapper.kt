@file:JvmSynthetic

package com.walletconnect.auth.client.mapper

import com.walletconnect.android_core.common.SDKError
import com.walletconnect.android_core.common.model.ConnectionState
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.common.json_rpc.payload.CacaoDTO
import com.walletconnect.auth.common.model.CacaoVO
import com.walletconnect.auth.common.model.IssuerVO
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.auth.signature.SignatureType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@JvmSynthetic
internal fun Auth.Model.AppMetaData.toEngineDO(): EngineDO.AppMetaData =
    EngineDO.AppMetaData(name, description, url, icons, redirect)

@JvmSynthetic
internal fun String.toVO(): IssuerVO = IssuerVO(this)

@JvmSynthetic
internal fun Auth.Params.Respond.toEngineDO(): EngineDO.Respond = when (this) {
    is Auth.Params.Respond.Result -> EngineDO.Respond.Result(id, signature)
    is Auth.Params.Respond.Error -> EngineDO.Respond.Error(id, code, message)
}

@JvmSynthetic
internal fun ConnectionState.toClient(): Auth.Event.ConnectionStateChange = Auth.Event.ConnectionStateChange(Auth.Model.ConnectionState(this.isAvailable))

@JvmSynthetic
internal fun SDKError.toClient(): Auth.Event.Error = Auth.Event.Error(Auth.Model.Error(this.exception))

@JvmSynthetic
internal fun EngineDO.Events.onAuthRequest.toClient(): Auth.Event.AuthRequest = Auth.Event.AuthRequest(id, message)

@JvmSynthetic
internal fun EngineDO.Events.onAuthResponse.toClient(): Auth.Event.AuthResponse = when (val response = response) {
    is EngineDO.AuthResponse.Error -> Auth.Event.AuthResponse(id, Auth.Model.Response.Error(id, response.code, response.message))
    is EngineDO.AuthResponse.Result -> Auth.Event.AuthResponse(id, Auth.Model.Response.Result(id, response.cacao.toClient()))
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
internal fun EngineDO.PayloadParams.toClient(): Auth.Model.PendingRequest.PayloadParams =
    Auth.Model.PendingRequest.PayloadParams(
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
internal fun Auth.Model.Cacao.Signature.toVO(): CacaoVO.SignatureVO = CacaoVO.SignatureVO(t, s, m)

@JvmSynthetic
internal fun Auth.Model.Cacao.Signature.toDTO(): CacaoDTO.SignatureDTO = CacaoDTO.SignatureDTO(t, s, m)

@JvmSynthetic
internal fun CacaoVO.toClient(): Auth.Model.Cacao = Auth.Model.Cacao(header.toClient(), payload.toClient(), signature.toClient())

@JvmSynthetic
internal fun List<EngineDO.PendingRequest>.toClient(): List<Auth.Model.PendingRequest> =
    map { request ->
        Auth.Model.PendingRequest(
            request.id,
            request.payloadParams.toClient(),
            request.message
        )
    }

@JvmSynthetic
internal fun EngineDO.Response.toClient(): Auth.Model.Response = when (this) {
    is EngineDO.Response.Result -> Auth.Model.Response.Result(id, cacao.toClient())
    is EngineDO.Response.Error -> Auth.Model.Response.Error(id, code, message)
}

@JvmSynthetic
internal fun CacaoVO.HeaderVO.toClient(): Auth.Model.Cacao.Header = Auth.Model.Cacao.Header(t)

@JvmSynthetic
internal fun CacaoVO.PayloadVO.toClient(): Auth.Model.Cacao.Payload = Auth.Model.Cacao.Payload(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)

//internal fun EngineDO.Cacao.Payload.toClient(): Auth.Model.Cacao.Payload =
//    Auth.Model.Cacao.Payload(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)

@JvmSynthetic
internal fun CacaoVO.SignatureVO.toClient(): Auth.Model.Cacao.Signature = Auth.Model.Cacao.Signature(t, s, m)

private const val ISO_8601_PATTERN = "uuuu-MM-dd'T'HH:mm:ssXXX"
private const val MESSAGE_TEMPLATE_VERSION = "1"