package com.walletconnect.notify.data.jwt

import com.squareup.moshi.Json

interface NotifyJwtBase {
    @Json(name = "act") val action: String
    @Json(name = "iat") val issuedAt: Long
    @Json(name = "exp") val expiration: Long
    @Json(name = "ksu") val keyserverUrl: String
}