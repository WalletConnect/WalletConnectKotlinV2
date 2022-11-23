@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {

    single { GetPendingRequestsUseCase(get(), get())}

    single { SignEngine(get(), get(), get(), get(), get(), get(), get(), get()) }
}