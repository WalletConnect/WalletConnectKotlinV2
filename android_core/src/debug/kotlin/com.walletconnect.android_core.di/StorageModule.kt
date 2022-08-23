package com.walletconnect.android_core.di

import android.content.Context
import android.content.SharedPreferences
import com.squareup.sqldelight.ColumnAdapter
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

    single<ColumnAdapter<List<String>, String>> {
        object : ColumnAdapter<List<String>, String> {

            override fun decode(databaseValue: String) =
                if (databaseValue.isBlank()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }

            override fun encode(value: List<String>) = value.joinToString(separator = ",")
        }
    }

    single {
        get<T>().jsonRpcHistoryQueries
    }

    single {
        JsonRpcHistory(get(named(AndroidCoreDITags.RPC_STORE)), get(), get())
    }
}