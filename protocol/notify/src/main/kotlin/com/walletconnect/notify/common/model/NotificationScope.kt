@file:JvmSynthetic

package com.walletconnect.notify.common.model

internal sealed class NotificationScope {
    abstract val name: String
    abstract val description: String
    abstract val id: String

    data class Remote(
        override val name: String,
        override val description: String,
        override val id: String,
    ) : NotificationScope()

    data class Cached(
        override val name: String,
        override val description: String,
        override val id: String,
        val isSelected: Boolean,
    ) : NotificationScope()
}