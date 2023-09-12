package com.walletconnect.notify.common.model

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ServerSubscription(
    val appDomain: String,
    val symKey: String,
    val account: String,
    val scope: List<String>,
    val expiry: Long,
) {
    val appDomainWithHttps = if (appDomain.contains(HTTPS_PREFIX)) appDomain else HTTPS_PREFIX + appDomain

    private companion object {
        const val HTTPS_PREFIX = "https://"
    }

}
