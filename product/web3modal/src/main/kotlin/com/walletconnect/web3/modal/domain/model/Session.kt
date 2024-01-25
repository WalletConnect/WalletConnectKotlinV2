package com.walletconnect.web3.modal.domain.model

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory

internal sealed class Session(
    open val address: String,
    open val chain: String,
) {
    @JsonClass(generateAdapter = true)
    data class WalletConnect(
        override val address: String,
        override val chain: String,
        val topic: String,
    ): Session(chain, address)

    @JsonClass(generateAdapter = true)
    data class Coinbase(
        override val chain: String,
        override val address: String
    ): Session(chain, address)

}

internal fun buildWeb3ModalMoshi(moshi: Moshi.Builder): Moshi = moshi.add(sessionAdapter).build()

internal val sessionAdapter = PolymorphicJsonAdapterFactory.of(Session::class.java, "type")
    .withSubtype(Session.WalletConnect::class.java, "wcsession")
    .withSubtype(Session.Coinbase::class.java, "coinbase")
    .withDefaultValue(null)
