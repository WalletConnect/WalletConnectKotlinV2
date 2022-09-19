@file:JvmSynthetic

package com.walletconnect.chat.discovery.keyserver.model

import com.squareup.moshi.Json

internal sealed class KeyServerDTO {
    data class Account(
        @Json(name = "account")
        val account: String,
        @Json(name = "publicKey")
        val publicKey: String,
    ) : KeyServerDTO()
}