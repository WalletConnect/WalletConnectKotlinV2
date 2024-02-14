package com.walletconnect.android.internal.common.signing.cacao

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.android.internal.common.signing.signature.Signature

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
    ) {
        @get:Throws(Exception::class)
        val actionsString get() = getActionsString()
        @get:Throws(Exception::class)
        val methods get() = resources.getActions()

        companion object {
            const val CURRENT_VERSION = "1"
            const val ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZZZZZ"
            const val RECAPS_PREFIX = "urn:recap:"
            const val ATT_KEY = "att"
        }
    }
}

@JvmSynthetic
internal fun Cacao.Signature.toSignature(): Signature = Signature.fromString(s)

fun Cacao.Payload.toCAIP222Message(chainName: String = "Ethereum"): String {
    var message = "$domain wants you to sign in with your $chainName account:\n${Issuer(iss).address}\n\n"
    if (statement != null) message += "$statement"
    if (resources?.find { r -> r.startsWith(RECAPS_PREFIX) } != null) {
        message += " I further authorize the stated URI to perform the following actions on my behalf: $actionsString.\n"
    } else if (statement != null) {
        message += "\n"
    }
    message += "\nURI: $aud\nVersion: $version\nChain ID: ${Issuer(iss).chainIdReference}\nNonce: $nonce\nIssued At: $iat"
    if (exp != null) message += "\nExpiration Time: $exp"
    if (nbf != null) message += "\nNot Before: $nbf"
    if (requestId != null) message += "\nRequest ID: $requestId"
    if (!resources.isNullOrEmpty()) {
        message += "\nResources:"
        resources.forEach { resource -> message += "\n- $resource" }
    }

    return message
}

private fun Cacao.Payload.getActionsString(): String {
    val map = resources.decodeReCaps().parseReCaps()
    if (map.isEmpty()) throw Exception("Decoded ReCaps map is empty")
    var result = ""
    var index = 1

    map.forEach { (key, values) ->
        val prefix = values.keys.firstOrNull()?.substringBefore('/') ?: ""
        val itemsFormatted = values.keys.sorted().joinToString(", ") { "'${it.substringAfter('/')}'" }

        result += if (index == map.size) {
            "($index) '$prefix': $itemsFormatted for '$key'"
        } else {
            "($index) '$prefix': $itemsFormatted for '$key', "
        }
        index++
    }

    return result
}