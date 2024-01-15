package com.walletconnect.web3.modal.client.models.request

import com.walletconnect.sign.client.Sign
import com.walletconnect.web3.modal.engine.coinbase.CoinbaseResult

sealed class SentRequestResult(
    open val method: String, open val params: String, open val chainId: String
) {

    data class WalletConnect(
        override val method: String,
        override val params: String,
        override val chainId: String,
        val requestId: Long,
        val sessionTopic: String
    ) : SentRequestResult(method, params, chainId)

    data class Coinbase(
        override val method: String,
        override val params: String,
        override val chainId: String,
        val results: List<CoinbaseResult>
    ) : SentRequestResult(method, params, chainId)
}

internal fun Sign.Model.SentRequest.toSentRequest() = SentRequestResult.WalletConnect(method, params, chainId, requestId, sessionTopic)
