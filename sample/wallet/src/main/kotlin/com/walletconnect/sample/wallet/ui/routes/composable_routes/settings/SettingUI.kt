package com.walletconnect.sample.wallet.ui.routes.composable_routes.settings


sealed interface Section {
    data class SettingsSection(val title: String, val items: List<Item>) : Section
    object LogoutSection : Section
}

sealed interface Item {
    data class SettingCopyableItem(val key: String, val value: String) : Item
}