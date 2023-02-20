package com.walletconnect.push.dapp.data

internal data class CastNotifyResponse(
    val sent: List<String>,
    val failed: List<Failed>,
    val notFound: List<String>
) {

    data class Failed(val account: String, val reason: String)
}
