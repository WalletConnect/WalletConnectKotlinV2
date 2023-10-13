@file:JvmSynthetic

package com.walletconnect.notify.common.model

internal sealed class NotificationScope {
    abstract val id: String
    abstract val name: String
    abstract val description: String

    data class Remote(
        override val id: String,
        override val name: String,
        override val description: String,
    ) : NotificationScope()

    data class Cached(
        override val id: String,
        override val name: String,
        override val description: String,
        val isSelected: Boolean,
    ) : NotificationScope()
}