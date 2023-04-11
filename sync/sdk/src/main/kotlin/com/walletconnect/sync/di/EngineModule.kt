@file:JvmSynthetic

package com.walletconnect.sync.di


import com.walletconnect.sync.engine.domain.SyncEngine
import com.walletconnect.sync.engine.use_case.*
import com.walletconnect.sync.engine.use_case.CreateUseCase
import com.walletconnect.sync.engine.use_case.DeleteUseCase
import com.walletconnect.sync.engine.use_case.GetStoresUseCase
import com.walletconnect.sync.engine.use_case.RegisterUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { CreateUseCase() }
    single { DeleteUseCase() }
    single { SetUseCase() }
    single { GetStoresUseCase() }
    single { RegisterUseCase() }
    single { SyncEngine(get(), get(), get(), get(), get()) }
}