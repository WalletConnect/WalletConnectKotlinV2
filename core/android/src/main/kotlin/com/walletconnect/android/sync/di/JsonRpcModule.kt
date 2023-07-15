@file:JvmSynthetic

package com.walletconnect.android.sync.di

import com.walletconnect.android.sync.common.json_rpc.JsonRpcMethod
import com.walletconnect.android.sync.common.json_rpc.SyncRpc
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module

@JvmSynthetic
internal fun jsonRpcModule() = module {
    addSerializerEntry(SyncRpc.SyncSet::class)
    addSerializerEntry(SyncRpc.SyncDelete::class)

    addDeserializerEntry(JsonRpcMethod.WC_SYNC_SET, SyncRpc.SyncSet::class)
    addDeserializerEntry(JsonRpcMethod.WC_SYNC_DELETE, SyncRpc.SyncDelete::class)
}