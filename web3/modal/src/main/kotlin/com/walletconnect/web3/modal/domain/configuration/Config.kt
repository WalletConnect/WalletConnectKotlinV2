package com.walletconnect.web3.modal.domain.configuration

import com.squareup.moshi.*
import com.walletconnect.web3.modal.client.Modal

internal const val CONFIGURATION = "modal_config"

@JsonClass(generateAdapter = true)
sealed class Config {

    @JsonClass(generateAdapter = true)
    data class Connect(
        @EncodedString
        val uri: String,
        val chains: List<String>? = null,
    ): Config()
}
