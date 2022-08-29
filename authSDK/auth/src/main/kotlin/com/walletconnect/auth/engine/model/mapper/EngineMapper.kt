package com.walletconnect.auth.engine.model.mapper

import com.walletconnect.android_core.common.model.MetaData
import com.walletconnect.android_core.common.model.Redirect
import com.walletconnect.auth.common.json_rpc.payload.CacaoDTO
import com.walletconnect.auth.common.json_rpc.payload.PayloadParamsDTO
import com.walletconnect.auth.common.model.PendingRequest
import com.walletconnect.auth.common.model.CacaoVO
import com.walletconnect.auth.common.model.IssuerVO
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.auth.signature.Signature

@JvmSynthetic
internal fun EngineDO.AppMetaData.toCore() =
    MetaData(name, description, url, icons, Redirect(redirect))

@JvmSynthetic
internal fun PayloadParamsDTO.toCacaoPayloadDTO(iss: IssuerVO): CacaoDTO.PayloadDTO = CacaoDTO.PayloadDTO(
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
internal fun PayloadParamsDTO.toFormattedMessage(iss: IssuerVO, chainName: String = "Ethereum"): String = this.toCacaoPayloadDTO(iss).toVO().toFormattedMessage(chainName)

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
internal fun CacaoDTO.toVO(): CacaoVO = CacaoVO(header.toVO(), payload.toVO(), signature.toVO())

@JvmSynthetic
internal fun CacaoDTO.HeaderDTO.toVO(): CacaoVO.HeaderVO = CacaoVO.HeaderVO(t)

@JvmSynthetic
internal fun CacaoDTO.PayloadDTO.toVO(): CacaoVO.PayloadVO = CacaoVO.PayloadVO(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)

internal fun CacaoDTO.PayloadDTO.toEngineDO(): EngineDO.Cacao.Payload =
    EngineDO.Cacao.Payload(iss, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)

@JvmSynthetic
internal fun CacaoDTO.SignatureDTO.toVO(): CacaoVO.SignatureVO = CacaoVO.SignatureVO(t, s, m)

@JvmSynthetic
internal fun CacaoVO.SignatureVO.toSignature(): Signature = Signature.fromString(s)

// todo: Figure out chain name resolving to support chain agnosticism
@JvmSynthetic
internal fun CacaoVO.PayloadVO.toFormattedMessage(chainName: String = "Ethereum"): String {
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
    EngineDO.PendingRequest(id, params.payloadParams.toEngineDO(), message)