package com.walletconnect.notify.common

import androidx.core.net.toUri

@JvmInline
internal value class NotifyServerUrl(val url: String = DEFAULT_NOTIFY_SERVER_URL) {
    fun toUri() = this.url.toUri()
}

const val DEFAULT_NOTIFY_SERVER_URL = "https://notify.walletconnect.com/"