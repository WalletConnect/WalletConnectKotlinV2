package com.walletconnect.auth.engine.mapper

import com.walletconnect.auth.common.model.*
import com.walletconnect.auth.signature.Signature

@JvmSynthetic
internal fun PayloadParams.toCacaoPayload(iss: Issuer): Cacao.Payload = Cacao.Payload(
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
internal fun PayloadParams.toFormattedMessage(iss: Issuer, chainName: String = "Ethereum"): String =
    this.toCacaoPayload(iss).toFormattedMessage(chainName)

@JvmSynthetic
internal fun Cacao.Signature.toSignature(): Signature = Signature.fromString(s)

// todo: Figure out chain name resolving to support chain agnosticism
@JvmSynthetic
internal fun Cacao.Payload.toFormattedMessage(chainName: String = "Ethereum"): String {
    var message = "$domain wants you to sign in with your $chainName account:\n$address\n"
    if (statement != null) message += "\n$statement\n"
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
internal fun JsonRpcHistoryEntry.toPendingRequest(issuer: Issuer): PendingRequest =
    PendingRequest(id, params.payloadParams, params.payloadParams.toFormattedMessage(issuer))