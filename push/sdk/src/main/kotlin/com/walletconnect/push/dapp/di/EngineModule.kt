package com.walletconnect.push.dapp.di

import com.walletconnect.push.dapp.engine.DappEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun dappEngineModule() = module {

    single { DappEngine(get(), get(), get(), get(), get(), get()) }

}