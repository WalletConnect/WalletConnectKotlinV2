package com.walletconnect.sign.di

import com.walletconnect.sign.engine.use_case.requests.OnSessionProposeUseCase
import org.koin.dsl.module

@JvmSynthetic
fun requestsModule() = module {

    single { OnSessionProposeUseCase(get(), get(), get(), get(), get()) }

}