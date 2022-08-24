package com.walletconnect.auth.signature

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.client.mapper.toClient
import com.walletconnect.auth.common.json_rpc.payload.CacaoDTO
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.util.bytesToHex
import com.walletconnect.utils.HexPrefix
import org.web3j.crypto.Sign
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// todo: Figure out chain name resolving to support chain agnosticism
fun Auth.Model.Cacao.Payload.toFormattedMessage(chainName: String = "Ethereum"): String {
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

fun Sign.SignatureData.toSignature(): Signature = Signature(v, r, s)
fun Signature.toCacaoSignature(): String = String.HexPrefix + r.bytesToHex() + s.bytesToHex() + v.bytesToHex()
fun Signature.toSignatureData(): Sign.SignatureData = Sign.SignatureData(v, r, s)

const val ISO_8601_PATTERN = "uuuu-MM-dd'T'HH:mm:ssXXX"