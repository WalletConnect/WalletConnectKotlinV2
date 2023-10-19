package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover


data class ExplorerApp(
    val id: String,
    val name: String,
    val homepage: String,
    val imageId: String,
    val description: String,
    val imageUrl: ImageUrl,
    val dappUrl: String,
    val isSubscribed: Boolean,
    val topic: String? = null,
) {
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(name, dappUrl, homepage)
        return matchingCombinations.any { it.contains(query, ignoreCase = true) }
    }
}

data class ImageUrl(
    val sm: String,
    val md: String,
    val lg: String,
)

