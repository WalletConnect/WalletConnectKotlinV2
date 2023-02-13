package com.walletconnect.push.dapp.data

data class CastNotifyResponse(
    val sent: List<String>,
    val failed: List<List<String>>,
    val notFound: List<String>
)
