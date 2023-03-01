@file:JvmSynthetic

package com.walletconnect.push.wallet.di

import com.walletconnect.push.wallet.engine.PushWalletEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun walletEngineModule() = module {

    single { PushWalletEngine(get(), get(), get(), get(), get(), get(), get()) }
}