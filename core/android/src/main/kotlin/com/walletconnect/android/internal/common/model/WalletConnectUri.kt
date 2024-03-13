@file:JvmSynthetic

package com.walletconnect.android.internal.common.model

import com.walletconnect.foundation.common.model.Topic

data class WalletConnectUri(
    val topic: Topic,
    val symKey: SymmetricKey,
    val relay: RelayProtocolOptions,
    val version: String = "2",
    val expiry: Expiry?,
    val methods: String?,
) {
    fun toAbsoluteString(): String =
        "wc:${topic.value}@$version?${getQuery()}&symKey=${symKey.keyAsHex}"

    private fun getQuery(): String {
        var query = "relay-protocol=${relay.protocol}"
        if (relay.data != null) {
            query = "$query&relay-data=${relay.data}"
        }
        if (expiry != null) {
            query = "$query&expiryTimestamp=${expiry.seconds}"
        }

        if (!methods.isNullOrEmpty()) {
            query = "$query&methods=$methods"
        }

        return query
    }
}