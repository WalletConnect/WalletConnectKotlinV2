package com.walletconnect.push.engine.sync

private const val NOTIFY_STORE_PREFIX = "com.walletconnect.notify."

enum class NotifySyncStores(val value: String) {
    NOTIFY_SUBSCRIPTION(NOTIFY_STORE_PREFIX + "notifySubscription"),
}