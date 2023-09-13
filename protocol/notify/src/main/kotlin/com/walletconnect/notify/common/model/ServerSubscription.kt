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
    val appDomainWithHttps = appDomain.ensureHttpsPrefix()
}

//TODO: REFACTOR ME
fun String.ensureHttpsPrefix(): String {
    val HTTPS_PREFIX = "https://"
    return if (this.contains(HTTPS_PREFIX)) this else HTTPS_PREFIX + this
}