package com.walletconnect.android_core.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.android_core.Database
import com.walletconnect.android_core.storage.JsonRpcHistory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

@SuppressLint("HardwareIds")
fun coreStorageModule(storageSuffix: String): Module = module {

    single<SharedPreferences>(named(DITags.RPC_STORE)) {
        val sharedPrefsFile = "wc_rpc_store$storageSuffix"

        androidContext().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)
    }

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = androidContext(),
            name = "WalletConnectV2$storageSuffix.db"
        )
    }

    single {
        get<Database>().jsonRpcHistoryQueries
    }

    single {
        JsonRpcHistory(get(named(DITags.RPC_STORE)), get())
    }
}