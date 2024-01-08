package com.walletconnect.web3.modal.client.models

import com.walletconnect.sign.client.Sign
import com.walletconnect.web3.modal.engine.coinbase.CoinbaseResult

data class Request(
    val method: String,
    val params: String,
    val expiry: Long? = null,
)

sealed class SentRequestResult(
    open val method: String, open val params: String, open val chainId: String
) {

    data class WC(
        override val method: String,
        override val params: String,
        override val chainId: String,
        val requestId: Long,
        val sessionTopic: String
    ) : SentRequestResult(method, params, chainId)

    data class Cb(
        override val method: String,
        override val params: String,
        override val chainId: String,
        val results: List<CoinbaseResult>
    ) : SentRequestResult(method, params, chainId)
}

internal fun Sign.Model.SentRequest.toSentRequest() = SentRequestResult.WC(method, params, chainId, requestId, sessionTopic)
