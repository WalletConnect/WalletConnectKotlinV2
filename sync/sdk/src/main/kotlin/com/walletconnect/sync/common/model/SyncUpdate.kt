@file:JvmSynthetic

package com.walletconnect.sync.common.model

internal sealed interface SyncUpdate {
    val id: Long
    val key: String

    data class SyncSet(override val id: Long, override val key: String, val value: String) : SyncUpdate

    data class SyncDelete(override val id: Long, override val key: String) : SyncUpdate
}