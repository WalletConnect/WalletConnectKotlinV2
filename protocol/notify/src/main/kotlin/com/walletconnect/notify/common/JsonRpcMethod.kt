@file:JvmSynthetic

package com.walletconnect.notify.common

internal object JsonRpcMethod {

    @get:JvmSynthetic
    const val WC_NOTIFY_MESSAGE: String = "wc_notifyMessage"

    @get:JvmSynthetic
    const val WC_NOTIFY_DELETE: String = "wc_notifyDelete"

    @get:JvmSynthetic
    const val WC_NOTIFY_SUBSCRIBE: String = "wc_notifySubscribe"

    @get:JvmSynthetic
    const val WC_NOTIFY_UPDATE: String = "wc_notifyUpdate"

    @get:JvmSynthetic
    const val WC_NOTIFY_WATCH_SUBSCRIPTIONS: String = "wc_notifyWatchSubscriptions"

    @get:JvmSynthetic
    const val WC_NOTIFY_SUBSCRIPTIONS_CHANGED: String = "wc_notifySubscriptionsChanged"
}