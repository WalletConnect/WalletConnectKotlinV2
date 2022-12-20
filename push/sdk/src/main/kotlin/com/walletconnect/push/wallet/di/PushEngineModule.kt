@file:JvmSynthetic

package com.walletconnect.push.wallet.di

import com.walletconnect.push.wallet.engine.domain.DecryptMessageUseCase
import com.walletconnect.push.wallet.engine.WalletEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun walletEngineModule() = module {

    single { DecryptMessageUseCase(get()) }

    single { WalletEngine(get(), get(), get(), get(), get()) }
}