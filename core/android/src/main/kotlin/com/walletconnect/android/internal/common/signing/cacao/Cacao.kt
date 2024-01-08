package com.walletconnect.android.internal.common.signing.cacao

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.cacao.SignatureInterface
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.ACTION_DELIMITER
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.ACTION_POSITION
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.ACTION_TYPE_POSITION
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.ATT_KEY
import com.walletconnect.android.internal.common.signing.cacao.Cacao.Payload.Companion.RECAPS_PREFIX
import com.walletconnect.android.internal.common.signing.signature.Signature
import org.bouncycastle.util.encoders.Base64
import org.json.JSONObject

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
        val actionsString get() = getActionsString(Issuer(iss))
        val methods get() = getActions(Issuer(iss))

        companion object {
            const val CURRENT_VERSION = "1"
            const val ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZZZZZ"
            const val RECAPS_PREFIX = "urn:recap:"
            const val ATT_KEY = "att"
            const val ACTION_TYPE_POSITION = 0
            const val ACTION_POSITION = 1
            const val ACTION_DELIMITER = "/"
        }
    }
}

@JvmSynthetic
internal fun Cacao.Signature.toSignature(): Signature = Signature.fromString(s)

fun Cacao.Payload.toCAIP222Message(chainName: String = "Ethereum"): String {
    var message = "$domain wants you to sign in with your $chainName account:\n${Issuer(iss).address}\n\n"
    if (statement != null) message += "$statement"
    if (resources?.find { r -> r.startsWith(RECAPS_PREFIX) } != null) message += " I further authorize the stated URI to perform the following actions on my behalf: (1) $actionsString for '${
        Issuer(iss).namespace
    }'\n"
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

private fun Cacao.Payload.getActionsString(issuer: Issuer): String {
    return decodeReCaps(issuer).entries.joinToString(", ") { "'${it.key}': " + it.value.joinToString(", ") { value -> "'$value'" } }
}

private fun Cacao.Payload.getActions(issuer: Issuer): List<String> {
    return decodeReCaps(issuer).values.flatten()
}

private fun Cacao.Payload.decodeReCaps(issuer: Issuer): MutableMap<String, MutableList<String>> {
    val encodedReCaps = resources?.find { resource -> resource.startsWith(RECAPS_PREFIX) }?.removePrefix(RECAPS_PREFIX) ?: throw Exception()
    val reCaps = Base64.decode(encodedReCaps).toString(Charsets.UTF_8)
    val requests = (JSONObject(reCaps).get(ATT_KEY) as JSONObject).getJSONArray(issuer.namespace)
    val actions: MutableMap<String, MutableList<String>> = mutableMapOf()

    for (i in 0 until requests.length()) {
        val actionString = requests.getJSONObject(i).keys().next() as String
        val actionType = actionString.split(ACTION_DELIMITER)[ACTION_TYPE_POSITION]
        val action = actionString.split(ACTION_DELIMITER)[ACTION_POSITION]
        actions[actionType]?.add(action) ?: actions.put(actionType, mutableListOf(action))
    }
    return actions
}