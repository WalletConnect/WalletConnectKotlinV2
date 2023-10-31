package com.walletconnect.sample.wallet.ui.common.subscriptions

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient

data class ActiveSubscriptionsUI(
    val topic: String,
    val icon: String,
    val name: String,
    val messageCount: Int,
    val description: String,
    val lastReceived: String,
    val appDomain: String,
    val hasUnreadMessages: Boolean,
    val isVerified: Boolean,
) {
    fun doesMatchSearchQuery(query: String): Boolean {
        return name.contains(query)
    }
}

fun Notify.Model.Subscription.toUI(): ActiveSubscriptionsUI = ActiveSubscriptionsUI(
    topic = topic,
    icon = metadata.icons.first(),
    name = metadata.name,
    messageCount = NotifyClient.getMessageHistory(params = Notify.Params.MessageHistory(topic)).size,
    appDomain = metadata.url,
    description = metadata.description,


    // todo: get actual values
    hasUnreadMessages = true,
    lastReceived = "",
    isVerified = false
)

fun List<Notify.Model.Subscription>.toUI(): List<ActiveSubscriptionsUI> {
    return this.map { subscription -> subscription.toUI() }
}