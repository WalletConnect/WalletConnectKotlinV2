package com.walletconnect.auth.di

import com.walletconnect.auth.json_rpc.data.JsonRpcSerializer
import org.koin.dsl.module
import com.walletconnect.android.impl.di.jsonRpcModule as coreJsonRpcModule

@JvmSynthetic
internal fun jsonRpcModule() = module {

    includes(coreJsonRpcModule())

    single {
        JsonRpcSerializer(get())
    }
}