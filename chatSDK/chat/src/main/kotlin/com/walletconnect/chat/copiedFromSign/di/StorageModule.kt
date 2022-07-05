package com.walletconnect.chat.copiedFromSign.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.chat.Database
import com.walletconnect.chat.copiedFromSign.storage.JsonRpcHistory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

@SuppressLint("HardwareIds")
@JvmSynthetic
internal fun storageModule(): Module = module {

    single<SharedPreferences>(named(DITags.RPC_STORE)) {
        val sharedPrefsFile = "wc_rpc_store"

        androidContext().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)
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

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = androidContext(),
            name = "WalletConnectV2.db"
        )
    }

    single {
        Database(get())
    }


    single {
        get<Database>().jsonRpcHistoryQueries
    }

    single {
        JsonRpcHistory(get(named(DITags.RPC_STORE)), get())
    }
}