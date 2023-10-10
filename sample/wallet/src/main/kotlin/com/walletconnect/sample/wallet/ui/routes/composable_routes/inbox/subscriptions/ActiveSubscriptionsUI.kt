package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.subscriptions
data class ActiveSubscriptionsUI(
    val topic: String,
    val icon: String,
    val name: String,
    val messageCount: Int,
    val description: String,
    val lastReceived: String,
    val appDomain: String,
    val hasUnreadMessages: Boolean,
) {
    fun doesMatchSearchQuery(query: String): Boolean {
        return name.contains(query)
    }
}