package com.walletconnect.notify.common

enum class Statement(val content: String) {
    SINGLE_APP_ONLY("I further authorize this app to send me notifications. Read more at https://walletconnect.com/notifications"),
    ALL_APPS("I further authorize this app to view and manage my notifications for ALL apps. Read more at https://walletconnect.com/notifications");

    companion object {
        fun toBoolean(content: String?): Boolean = when (content) {
            ALL_APPS.content -> true
            SINGLE_APP_ONLY.content -> false
            else -> throw IllegalArgumentException("Unknown statement: $content")
        }

        fun fromBoolean(allApps: Boolean): Statement = if (allApps) ALL_APPS else SINGLE_APP_ONLY
    }
}