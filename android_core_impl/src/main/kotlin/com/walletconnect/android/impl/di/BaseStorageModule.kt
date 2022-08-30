package com.walletconnect.android.impl.di

import com.walletconnect.android.impl.Database
import com.walletconnect.android.impl.storage.JsonRpcHistory
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