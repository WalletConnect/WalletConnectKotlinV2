package com.walletconnect.push.dapp.di

import com.walletconnect.push.dapp.engine.PushDappEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun dappEngineModule() = module {

    single {
        PushDappEngine(get(), get(), get(), get(), get(), get(), get())
    }
}