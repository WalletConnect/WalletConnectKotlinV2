package com.walletconnect.auth.di

import com.walletconnect.auth.use_case.responses.OnAuthRequestResponseUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun responsesModule() = module {
    single { OnAuthRequestResponseUseCase(cacaoVerifier = get(), pairingHandler = get(), pairingInterface = get()) }
}