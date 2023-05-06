package com.walletconnect.android.sync.di

import com.walletconnect.android.di.AndroidCoreDITags
import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import com.walletconnect.android.sync.storage.AccountsStorageRepository
import com.walletconnect.android.sync.storage.StoresStorageRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun syncStorageModule() = module {

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).accountsQueries }
    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).storesQueries }
    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).storeValuesQueries }

    single { AccountsStorageRepository(get()) }
    single { StoresStorageRepository(get(), get()) }
}