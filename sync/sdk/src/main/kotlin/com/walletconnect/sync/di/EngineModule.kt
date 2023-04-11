@file:JvmSynthetic

package com.walletconnect.sync.di


import com.walletconnect.sync.engine.domain.SyncEngine
import com.walletconnect.sync.engine.use_case.*
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { CreateUseCase() }
    single { DeleteUseCase() }
    single { SetUseCase() }
    single { GetStoresUseCase() }
    single { RegisterUseCase(get()) }
    single { SyncEngine(get(), get(), get(), get(), get()) }
}