@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.chat.engine.domain.ChatEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { ChatEngine(get(), get(), get(), get(), get()) }
}