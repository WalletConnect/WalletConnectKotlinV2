package com.walletconnect.sign.di

import com.walletconnect.sign.engine.use_case.requests.OnSessionProposeUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionRequestUseCase
import com.walletconnect.sign.engine.use_case.requests.OnSessionSettleUseCase
import org.koin.dsl.module

@JvmSynthetic
fun requestsModule() = module {

    single { OnSessionProposeUseCase(get(), get(), get(), get(), get()) }

    single { OnSessionSettleUseCase(get(), get(), get(), get(), get(), get(), get()) }

    single { OnSessionRequestUseCase(get(), get(), get(), get(), get()) }
}