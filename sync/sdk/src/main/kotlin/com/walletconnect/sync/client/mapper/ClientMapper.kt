@file:JvmSynthetic

package com.walletconnect.sync.client.mapper

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.sync.client.Sync
import com.walletconnect.sync.common.model.Events
import com.walletconnect.sync.common.model.Store
import com.walletconnect.sync.common.model.SyncUpdate

@JvmSynthetic
internal fun SDKError.toClientError(): Sync.Model.Error = Sync.Model.Error(this.exception)

@JvmSynthetic
internal fun ConnectionState.toClient(): Sync.Model.ConnectionState = Sync.Model.ConnectionState(isAvailable)

@JvmSynthetic
internal fun Events.OnSyncUpdate.toClient(): Sync.Events.OnSyncUpdate = Sync.Events.OnSyncUpdate(store.toClient(), update.toClient())

@JvmSynthetic
internal fun Store.toClient(): Sync.Type.Store = Sync.Type.Store(value)

@JvmSynthetic
internal fun SyncUpdate.toClient(): Sync.Model.SyncUpdate = when (this) {
    is SyncUpdate.SyncDelete -> Sync.Model.SyncUpdate.SyncDelete(id, key)
    is SyncUpdate.SyncSet -> Sync.Model.SyncUpdate.SyncSet(id, key, value)
}
