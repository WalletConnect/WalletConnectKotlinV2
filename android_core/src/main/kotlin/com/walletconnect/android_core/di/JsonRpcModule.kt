package com.walletconnect.android_core.di

import com.walletconnect.android_core.json_rpc.data.NetworkState
import org.koin.dsl.module

fun jsonRpcModule() = module {

    single {
        NetworkState(get())
    }
}