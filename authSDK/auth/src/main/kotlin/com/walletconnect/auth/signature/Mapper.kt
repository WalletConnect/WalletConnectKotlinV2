package com.walletconnect.auth.signature

import com.walletconnect.auth.client.Auth
import com.walletconnect.util.bytesToHex
import com.walletconnect.utils.HexPrefix
import org.web3j.crypto.Sign
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun Auth.Model.Cacao.Payload.toFormattedMessage(): String {
    var message = "$domain wants you to sign in with your Ethereum account:\n$address\n\n"
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

fun Auth.Params.Request.toCacaoPayload(iss: String): Auth.Model.Cacao.Payload = Auth.Model.Cacao.Payload(
    iss = iss,
    domain = domain,
    aud = aud,
    version = "1",  // Specs don't describe how to handle versioning
    nonce = nonce,
    iat = DateTimeFormatter.ofPattern(ISO_8601_PATTERN).format(ZonedDateTime.now()),
    nbf = nbf,
    exp = exp,
    statement = statement,
    requestId = requestId,
    resources = resources,
)

fun Sign.SignatureData.toSignature(): Signature = Signature(v, r, s)
fun Auth.Model.Cacao.Signature.toSignature(): Signature = Signature.fromString(s)
fun Signature.toSignatureData(): Sign.SignatureData = Sign.SignatureData(v, r, s)
fun Signature.toCacaoSignature(): String = String.HexPrefix + r.bytesToHex() + s.bytesToHex() + v.bytesToHex()

const val ISO_8601_PATTERN = "uuuu-MM-dd'T'HH:mm:ssXXX"

