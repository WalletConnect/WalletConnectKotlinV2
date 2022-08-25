package com.walletconnect.auth.engine.model.mapper

import com.walletconnect.android_core.common.model.MetaData
import com.walletconnect.android_core.common.model.Redirect
import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.mapper.toClient
import com.walletconnect.auth.common.json_rpc.payload.CacaoDTO
import com.walletconnect.auth.common.json_rpc.payload.PayloadParamsDTO
import com.walletconnect.auth.common.model.PendingRequest
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.auth.signature.Signature

@JvmSynthetic
internal fun EngineDO.AppMetaData.toCore() =
    MetaData(name, description, url, icons, Redirect(redirect))

@JvmSynthetic
internal fun PayloadParamsDTO.toCacaoPayloadDTO(iss: EngineDO.Issuer): CacaoDTO.PayloadDTO = CacaoDTO.PayloadDTO(
    iss.value,
    domain = domain,
    aud = aud,
    version = version,
    nonce = nonce,
    iat = iat,
    nbf = nbf,
    exp = exp,
    statement = statement,
    requestId = requestId,
    resources = resources
)

@JvmSynthetic
internal fun PayloadParamsDTO.toFormattedMessage(iss: EngineDO.Issuer, chainName: String = "Ethereum"): String =
    this.toCacaoPayloadDTO(iss).toEngineDO().toFormattedMessage(chainName)

@JvmSynthetic
internal fun EngineDO.PayloadParams.toDTO(): PayloadParamsDTO = PayloadParamsDTO(
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
    resources = resources
)

@JvmSynthetic
internal fun PayloadParamsDTO.toEngineDO(): EngineDO.PayloadParams = EngineDO.PayloadParams(
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
    resources = resources
)

@JvmSynthetic
internal fun CacaoDTO.toEngineDO(): EngineDO.Cacao = EngineDO.Cacao(header.toEngineDO(), payload.toEngineDO(), signature.toEngineDO())

@JvmSynthetic
internal fun CacaoDTO.HeaderDTO.toEngineDO(): EngineDO.Cacao.Header = EngineDO.Cacao.Header(t)

@JvmSynthetic
internal fun CacaoDTO.PayloadDTO.toEngineDO(): EngineDO.Cacao.Payload =
    EngineDO.Cacao.Payload(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)

@JvmSynthetic
internal fun CacaoDTO.SignatureDTO.toEngineDO(): EngineDO.Cacao.Signature = EngineDO.Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun EngineDO.Cacao.Signature.toSignature(): Signature = Signature.fromString(s)

// todo: Figure out chain name resolving to support chain agnosticism
@JvmSynthetic
internal fun EngineDO.Cacao.Payload.toFormattedMessage(chainName: String = "Ethereum"): String {
    var message = "$domain wants you to sign in with your $chainName account:\n$address\n\n"
    if (statement != null) message += "$statement\n"
    message += "\nURI: $aud\nVersion: $version\nChain ID: $chainId\nNonce: $nonce\nIssued At: $iat"
    if (exp != null) message += "\nExpiration Time: $exp"
    if (nbf != null) message += "\nNot Before: $nbf"
    if (requestId != null) message += "\nRequest ID: $requestId"
    if (!resources.isNullOrEmpty()) {
        message += "\nResources:"
        resources.forEach { resource -> message += "\n- $resource" }
    }
    return message
}

@JvmSynthetic
internal fun EngineDO.WalletConnectUri.toAbsoluteString(): String =
    "wc:auth-${topic.value}@$version?${getQuery()}&symKey=${symKey.keyAsHex}"

private fun EngineDO.WalletConnectUri.getQuery(): String {
    var query = "relay-protocol=${relay.protocol}"
    if (relay.data != null) {
        query = "$query&relay-data=${relay.data}"
    }
    return query
}

@JvmSynthetic
internal fun PendingRequest.toEngineDO(message: String): EngineDO.PendingRequest =
    EngineDO.PendingRequest(id, payloadParams.toEngineDO(), message)