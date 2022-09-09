package com.walletconnect.auth.di

import com.walletconnect.auth.json_rpc.data.JsonRpcSerializer
import com.walletconnect.auth.json_rpc.domain.JsonRpcInteractor
import org.koin.dsl.module

@JvmSynthetic
internal fun jsonRpcModule() = module {

    single {
        JsonRpcSerializer(get())
    }

    single {
        JsonRpcInteractor(get(), get(), get(), get())
    }
}