package com.walletconnect.auth.di

import com.walletconnect.auth.use_case.requests.OnAuthRequestUseCase
import org.koin.dsl.module

fun requestsModule() = module {
    single { OnAuthRequestUseCase(get(), get(), get()) }
}
