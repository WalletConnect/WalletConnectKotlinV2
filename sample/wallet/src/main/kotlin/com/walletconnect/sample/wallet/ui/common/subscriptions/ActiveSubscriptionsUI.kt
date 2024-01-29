package com.walletconnect.sample.wallet.ui.common.subscriptions

import com.walletconnect.notify.client.Notify
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
        val matchingCombinations = listOf(name, appDomain)
        return matchingCombinations.any { it.contains(query, ignoreCase = true) }
    }
}

fun Notify.Model.Subscription.toUI(): ActiveSubscriptionsUI = ActiveSubscriptionsUI(
    topic = topic,
    imageUrl = metadata.icons.toImageUrl(),
    name = metadata.name,
    appDomain = metadata.url,
    description = metadata.description,


    // todo: get actual values
    messageCount = 0,
    hasUnreadMessages = false,
    lastReceived = "",
    isVerified = false
)

fun List<Notify.Model.Subscription>.toUI(): List<ActiveSubscriptionsUI> {
    return this.map { subscription -> subscription.toUI() }
}