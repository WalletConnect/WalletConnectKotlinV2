package com.walletconnect.web3.wallet.ui

sealed interface PushWalletEvent

data class PushRequest(val requestId: String, val peerName: String, val peerDesc: String, val icon: String?, val redirect: String?): PushWalletEvent

object PushRequestResponded : PushWalletEvent

data class PushMessage(val title: String, val body: String, val icon: String?, val url: String?): PushWalletEvent