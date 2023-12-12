package com.walletconnect.sample.wallet.ui.common.subscriptions

import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.ui.common.ImageUrl
import com.walletconnect.sample.wallet.ui.common.toImageUrl

data class ActiveSubscriptionsUI(
    val topic: String,
    val imageUrl: ImageUrl,
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
    imageUrl = metadata.icons.toImageUrl(),
    name = metadata.name,
    messageCount = NotifyClient.getNotificationHistory(params = Notify.Params.NotificationHistory(topic)).size,
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