@file:JvmSynthetic

package com.walletconnect.notify.common.model

internal sealed class Scope {
    abstract val id: String
    abstract val name: String
    abstract val description: String

    data class Remote(
        override val id: String,
        override val name: String,
        override val description: String,
        val iconUrl: String?,
    ) : Scope()

    data class Cached(
        override val id: String,
        override val name: String,
        override val description: String,
        val isSelected: Boolean,
    ) : Scope()
}