package com.walletconnect.auth.di

import com.walletconnect.auth.use_case.responses.OnAuthRequestResponseUseCase
import org.koin.dsl.module

fun responsesModule() = module {
    single { OnAuthRequestResponseUseCase(get(), get(), get()) }
}