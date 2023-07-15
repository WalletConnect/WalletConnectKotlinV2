package com.walletconnect.web3.inbox.webview

@JvmInline
value class WebViewQueryParams(val value: Map<String, String>) {
    override fun toString(): String = value.entries.joinToString("&", prefix = "?") { (key, value) -> "$key=$value" }
}