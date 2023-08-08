package com.walletconnect.sample.wallet.ui

sealed interface NotifyEvent

data class NotifyMessage(val title: String, val body: String, val icon: String?, val url: String?) : NotifyEvent