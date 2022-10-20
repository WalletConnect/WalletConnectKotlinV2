package com.walletconnect.android.impl.di

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.android.impl.Database
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

inline fun <reified T : Database> coreStorageModule(databaseSchema: SqlDriver.Schema, storageSuffix: String) = module {

    includes(baseStorageModule<T>())

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = databaseSchema,
            context = androidContext(),
            name = "WalletConnectV2$storageSuffix.db"
        )
    }
}