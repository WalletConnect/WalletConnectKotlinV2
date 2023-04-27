package com.walletconnect.android.sync.di

import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import com.walletconnect.android.sync.storage.AccountsStorageRepository
import com.walletconnect.android.sync.storage.StoresStorageRepository
import org.koin.dsl.module

@JvmSynthetic
internal fun syncStorageModule() = module {

    single { get<AndroidCoreDatabase>().accountsQueries }
    single { get<AndroidCoreDatabase>().storesQueries }
    single { get<AndroidCoreDatabase>().storeValuesQueries }

    single { AccountsStorageRepository(get()) }
    single { StoresStorageRepository(get(), get()) }
}