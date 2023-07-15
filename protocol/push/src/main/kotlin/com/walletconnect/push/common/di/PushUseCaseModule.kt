@file:JvmSynthetic

package com.walletconnect.push.common.di

import com.walletconnect.push.common.domain.ExtractPushConfigUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun pushEngineUseCaseModules() = module {

    single {
        ExtractPushConfigUseCase(serializer = get())
    }
}