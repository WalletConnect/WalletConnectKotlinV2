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
internal fun PayloadParams.toCAIP122Message(iss: Issuer, chainName: String = "Ethereum"): String =
    this.toCacaoPayload(iss).toCAIP122Message(chainName)

@JvmSynthetic
internal fun Cacao.Signature.toSignature(): Signature = Signature.fromString(s)

@JvmSynthetic
internal fun Cacao.Payload.toCAIP122Message(chainName: String = "Ethereum"): String {
    var message = "$domain wants you to sign in with your $chainName account:\n${issuer.address}\n"
    if (statement != null) message += "\n$statement\n"
    message += "\nURI: $aud\nVersion: $version\nChain ID: ${issuer.chainIdReference}\nNonce: $nonce\nIssued At: $iat"
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
internal fun JsonRpcHistoryEntry.toPendingRequest(): PendingRequest = PendingRequest(id, params.payloadParams)