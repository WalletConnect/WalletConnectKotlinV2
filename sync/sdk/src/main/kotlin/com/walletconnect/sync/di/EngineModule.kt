@file:JvmSynthetic

package com.walletconnect.sync.di


import com.walletconnect.sync.engine.domain.SyncEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { SyncEngine() }
}