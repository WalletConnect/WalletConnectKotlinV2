package com.walletconnect.web3.modal.data.model

import com.squareup.moshi.Json

internal data class IdentityDTO(
    @Json(name = "name")
    val name: String?,
    @Json(name = "avatar")
    val avatar: String?
)
