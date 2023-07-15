@file:JvmSynthetic

package com.walletconnect.android.sync.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.type.EngineEvent

sealed class Events : EngineEvent {
    data class OnSyncUpdate(val accountId: AccountId, val store: Store, val update: SyncUpdate) : Events()
}