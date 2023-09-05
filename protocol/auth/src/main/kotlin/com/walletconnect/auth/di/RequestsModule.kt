package com.walletconnect.auth.di

import com.walletconnect.auth.use_case.requests.OnAuthRequestUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun requestsModule() = module {
    single { OnAuthRequestUseCase(jsonRpcInteractor = get(), resolveAttestationIdUseCase =  get()) }
}
