@file:JvmSynthetic

package com.walletconnect.android.internal

import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.WalletConnectUri
import com.walletconnect.foundation.common.model.Topic
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder


internal object Validator {

    private const val WC_URI_QUERY_KEY = "wc?uri="
    @JvmSynthetic
    internal fun validateWCUri(uri: String): WalletConnectUri? {
        val wcUri = getWcUri(uri)
        if (!wcUri.startsWith("wc:")) return null

        val properUriString = when {
            wcUri.contains("wc://") -> wcUri
            wcUri.contains("wc:/") -> wcUri.replace("wc:/", "wc://")
            else -> wcUri.replace("wc:", "wc://")
        }

        val pairUri: URI = try {
            URI(properUriString)
        } catch (e: URISyntaxException) {
            return null
        }

        if (pairUri.userInfo.isEmpty()) return null
        val mapOfQueryParameters: Map<String, String> =
            pairUri.query.split("&").associate { query -> query.substringBefore("=") to query.substringAfter("=") }

//        if (!mapOfQueryParameters.containsKey("methods")) return null TODO: Will add back later after discussion about how we want to handle registered methods

        var relayProtocol = ""
        mapOfQueryParameters["relay-protocol"]?.let { relayProtocol = it } ?: return null
        if (relayProtocol.isEmpty()) return null

        val relayData: String? = mapOfQueryParameters["relay-data"]

        val expiry: String? = mapOfQueryParameters["expiryTimestamp"]

        var symKey = ""
        mapOfQueryParameters["symKey"]?.let { symKey = it } ?: return null
        if (symKey.isEmpty()) return null

        return WalletConnectUri(
            topic = Topic(pairUri.userInfo),
            relay = RelayProtocolOptions(protocol = relayProtocol, data = relayData),
            symKey = SymmetricKey(symKey),
            expiry = if (expiry != null) Expiry(expiry.toLong()) else null
            /*registeredMethods = mapOfQueryParameters["methods"]!!*/ //TODO: Will add back later after discussion about how we want to handle registered methods
        )
    }

    @JvmSynthetic
    internal fun doesNotContainRegisteredMethods(uriMethods: String, registeredMethods: Set<String>): Boolean {
        return !registeredMethods.containsAll(uriMethods.split(","))
    }

    private fun getWcUri(uriScheme: String): String {
        return try {
            val uri = if (uriScheme.contains(WC_URI_QUERY_KEY)) {
                uriScheme.split(WC_URI_QUERY_KEY)[1]
            } else {
                uriScheme
            }
            URLDecoder.decode(uri, "UTF-8")
        } catch (e: Throwable) {
            uriScheme
        }
    }
}
