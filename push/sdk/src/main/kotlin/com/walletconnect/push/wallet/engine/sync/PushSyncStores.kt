package com.walletconnect.push.wallet.engine.sync

private const val PUSH_STORE_PREFIX = "com.walletconnect.notify."

enum class PushSyncStores(val value: String) {
    PUSH_SUBSCRIPTION(PUSH_STORE_PREFIX + "pushSubscription"),
}