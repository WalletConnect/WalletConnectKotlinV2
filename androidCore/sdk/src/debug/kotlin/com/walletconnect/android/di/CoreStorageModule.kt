package com.walletconnect.android.di

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.android.internal.common.di.DatabaseConfig
import com.walletconnect.android.internal.common.di.baseStorageModule
import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import com.walletconnect.utils.Empty
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun coreStorageModule(storagePrefix: String = String.Empty) = module {

    single { DatabaseConfig(storagePrefix) }

    includes(baseStorageModule())

    single<SqlDriver>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE_DRIVER)) {
        AndroidSqliteDriver(
            schema = AndroidCoreDatabase.Schema,
            context = androidContext(),
            name = get<DatabaseConfig>().ANDROID_CORE_DB_NAME,
        )
    }
}

fun sdkBaseStorageModule(databaseSchema: SqlDriver.Schema, databaseName: String) = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = databaseSchema,
            context = androidContext(),
            name = databaseName,
        )
    }
}