@file:JvmSynthetic

package com.walletconnect.android.di


import com.walletconnect.android.test.json_rpc.JsonRpcMethod
import com.walletconnect.android.test.json_rpc.TestHistoryRPC
import com.walletconnect.utils.addDeserializerEntry
import com.walletconnect.utils.addSerializerEntry
import org.koin.dsl.module

@JvmSynthetic
internal fun testJsonRpcModule() = module {
    addSerializerEntry(TestHistoryRPC::class)

    addDeserializerEntry(JsonRpcMethod.TEST_HISTORY, TestHistoryRPC::class)
}