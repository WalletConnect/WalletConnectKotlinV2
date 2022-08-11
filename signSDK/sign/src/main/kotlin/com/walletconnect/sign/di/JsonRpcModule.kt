package com.walletconnect.sign.di

import com.walletconnect.sign.json_rpc.data.JsonRpcSerializer
import com.walletconnect.sign.json_rpc.domain.JsonRpcInteractor
import org.koin.dsl.module
import com.walletconnect.android_core.di.jsonRpcModule as androidCoreJsonRcpModule

fun jsonRpcModule() = module {

    includes(androidCoreJsonRcpModule())

    single {
        JsonRpcSerializer(get())
    }

    single {
        JsonRpcInteractor(get(), get(), get(), get(), get())
    }
}