package com.walletconnect.web3.modal.domain.model

import com.squareup.moshi.JsonClass

internal sealed class Session {
    abstract val address: String
    abstract val chain: String

    @JsonClass(generateAdapter = true)
    data class WalletConnect(
        override val address: String,
        override val chain: String,
        val topic: String,
    ) : Session()

    @JsonClass(generateAdapter = true)
    data class Coinbase(
        override val chain: String,
        override val address: String,
    ) : Session()
}