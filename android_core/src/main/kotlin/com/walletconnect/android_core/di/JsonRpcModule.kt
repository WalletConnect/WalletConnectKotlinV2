package com.walletconnect.android_core.di

import org.koin.dsl.module

//relayerModule in android_core
@JvmSynthetic
fun jsonRpcModule() = module {
    single {
        JsonRpcSerializer(get())
    }

    single {
        NetworkState(get())
    }

    //todo: change name to JsonRpcInteractor
    single {
        RelayerInteractor(get(), get(), get(), get(), get())
    }
}