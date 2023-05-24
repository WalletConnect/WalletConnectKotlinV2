package com.walletconnect.web3.modal.domain.configuration

import com.squareup.moshi.*

internal const val CONFIG_ARG = "modal_config_arg"

@JsonClass(generateAdapter = true)
sealed class Config {

    @JsonClass(generateAdapter = true)
    data class Connect(
        @EncodedString
        val uri: String,
        val chains: List<String>? = null,
    ): Config()
}

internal fun Config.asArg() = Web3ModalConfigSerializer.serialize(this)
