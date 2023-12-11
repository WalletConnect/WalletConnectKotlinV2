package com.walletconnect.web3.modal.domain.model

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory

sealed class Session {
    @JsonClass(generateAdapter = true)
    data class WalletConnectSession(
        val chain: String,
        val address: String
    ): Session()

    @JsonClass(generateAdapter = true)
    data class CoinbaseSession(
        val topic: String,
        val chain: String
    ): Session()

    object Invalid: Session()
}

internal fun buildWeb3ModalMoshi(moshi: Moshi) = moshi.newBuilder().add(sessionAdapter).build()

internal val sessionAdapter = PolymorphicJsonAdapterFactory.of(Session::class.java, "type")
    .withSubtype(Session.WalletConnectSession::class.java, "wcsession")
    .withSubtype(Session.CoinbaseSession::class.java, "coinbase")
    .withDefaultValue(Session.Invalid)


//{"chain":"eth","networkId":1,"address":"0x8ea13985153989d9ebDB94dC45F46398c4f6858c"}