@file:JvmSynthetic

package com.walletconnect.android.sync.common.model

sealed interface SyncUpdate {
    val id: Long
    val key: String

    // https://github.com/WalletConnect/WalletConnectKotlinV2/issues/800 -> update params to have StoreKey and StoreValue
    data class SyncSet(override val id: Long, override val key: String, val value: String) : SyncUpdate

    // https://github.com/WalletConnect/WalletConnectKotlinV2/issues/800 -> update params to have StoreKey
    data class SyncDelete(override val id: Long, override val key: String) : SyncUpdate
}