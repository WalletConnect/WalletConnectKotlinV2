package com.walletconnect.android_core.di

import com.walletconnect.android_core.Database
import com.walletconnect.android_core.storage.JsonRpcHistory
import org.koin.core.qualifier.named
import org.koin.dsl.module

inline fun <reified T: Database> baseStorageModule() = module {

    single {
        get<T>().jsonRpcHistoryQueries
    }

    single {
        JsonRpcHistory(get(named(AndroidCoreDITags.RPC_STORE)), get(), get())
    }
}