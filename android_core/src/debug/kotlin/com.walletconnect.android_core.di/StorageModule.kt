package com.walletconnect.android_core.di

import android.content.Context
import android.content.SharedPreferences
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.android_core.Database
import com.walletconnect.android_core.storage.JsonRpcHistory
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

inline fun <reified T: Database> coreStorageModule(databaseSchema: SqlDriver.Schema, storageSuffix: String) = module {

    single<SharedPreferences>(named(AndroidCoreDITags.RPC_STORE)) {
        val sharedPrefsFile = "wc_rpc_store$storageSuffix"

        androidContext().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)
    }

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = databaseSchema,
            context = androidContext(),
            name = "WalletConnectV2$storageSuffix.db"
        )
    }

    single {
        get<T>().jsonRpcHistoryQueries
    }

    single {
        JsonRpcHistory(get(named(AndroidCoreDITags.RPC_STORE)), get(), get())
    }
}