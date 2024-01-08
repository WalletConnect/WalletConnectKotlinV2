package com.walletconnect.web3.modal.engine.coinbase

sealed class CoinbaseResult {
    class Result(val value: String) : CoinbaseResult()

    class Error(val code: Long, val message: String) : CoinbaseResult()
}