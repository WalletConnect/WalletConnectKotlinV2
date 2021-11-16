package org.walletconnect.example.payloads

data class EthSendTransaction(
    val from: String,
    val to: String,
    val `data`: String,
    val gas: String,
    val gasPrice: String,
    val value: String,
    val nonce: String
)