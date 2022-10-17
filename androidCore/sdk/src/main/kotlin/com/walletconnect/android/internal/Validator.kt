package com.walletconnect.android.internal

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.walletconnect.android.internal.common.model.WalletConnectUri
import com.walletconnect.foundation.common.model.Topic
import java.net.URI
import java.net.URISyntaxException

internal object Validator {

    @JvmSynthetic
    internal fun validateWCUri(uri: String): WalletConnectUri? {
        if (!uri.startsWith("wc:")) return null

        val properUriString = when {
            uri.contains("wc://") -> uri
            uri.contains("wc:/") -> uri.replace("wc:/", "wc://")
            else -> uri.replace("wc:", "wc://")
        }

        val pairUri: URI = try {
            URI(properUriString)
        } catch (e: URISyntaxException) {
            return null
        }

        if (pairUri.userInfo.isEmpty()) return null
        val mapOfQueryParameters: Map<String, String> =
            pairUri.query.split("&").associate { query -> query.substringBefore("=") to query.substringAfter("=") }

        if (!mapOfQueryParameters.containsKey("methods")) return null

        var relayProtocol = ""
        mapOfQueryParameters["relay-protocol"]?.let { relayProtocol = it } ?: return null
        if (relayProtocol.isEmpty()) return null

        val relayData: String? = mapOfQueryParameters["relay-data"]

        var symKey = ""
        mapOfQueryParameters["symKey"]?.let { symKey = it } ?: return null
        if (symKey.isEmpty()) return null

        return WalletConnectUri(
            topic = Topic(pairUri.userInfo),
            relay = RelayProtocolOptions(protocol = relayProtocol, data = relayData),
            symKey = SymmetricKey(symKey),
            registeredMethods = mapOfQueryParameters["methods"]!!
        )
    }

    fun doesNotContainRegisteredMethods(uriMethods: String, registeredMethods: Set<String>): Boolean {
        return !registeredMethods.containsAll(uriMethods.split(","))
    }
}