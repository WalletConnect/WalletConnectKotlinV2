package com.walletconnect.android.internal.common.cacao

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.internal.common.cacao.signature.Signature

@JsonClass(generateAdapter = true)
data class Cacao(
    @Json(name = "h")
    val header: Header,
    @Json(name = "p")
    val payload: Payload,
    @Json(name = "s")
    val signature: Signature,
) {
    @Keep
    @JsonClass(generateAdapter = true)
    data class Signature(
        @Json(name = "t")
        override val t: String,
        @Json(name = "s")
        override val s: String,
        @Json(name = "m")
        override val m: String? = null,
    ) : SignatureInterface

    @JsonClass(generateAdapter = true)
    data class Header(
        @Json(name = "t")
        val t: String,
    )

    @JsonClass(generateAdapter = true)
    data class Payload(
        @Json(name = "iss")
        val iss: String,
        @Json(name = "domain")
        val domain: String,
        @Json(name = "aud")
        val aud: String,
        @Json(name = "version")
        val version: String,
        @Json(name = "nonce")
        val nonce: String,
        @Json(name = "iat")
        val iat: String,
        @Json(name = "nbf")
        val nbf: String?,
        @Json(name = "exp")
        val exp: String?,
        @Json(name = "statement")
        val statement: String?,
        @Json(name = "requestId")
        val requestId: String?,
        @Json(name = "resources")
        val resources: List<String>?,
    )
}

@JvmSynthetic
internal fun Cacao.Signature.toSignature(): Signature = Signature.fromString(s)

@JvmSynthetic
fun Cacao.Payload.toCAIP122Message(chainName: String = "Ethereum"): String {
    var message = "$domain wants you to sign in with your $chainName account:\n${Issuer(iss).address}\n"
    if (statement != null) message += "\n$statement\n"
    message += "\nURI: $aud\nVersion: $version\nChain ID: ${Issuer(iss).chainIdReference}\nNonce: $nonce\nIssued At: $iat"
    if (exp != null) message += "\nExpiration Time: $exp"
    if (nbf != null) message += "\nNot Before: $nbf"
    if (requestId != null) message += "\nRequest ID: $requestId"
    if (!resources.isNullOrEmpty()) {
        message += "\nResources:"
        resources!!.forEach { resource -> message += "\n- $resource" }
    }
    return message
}
