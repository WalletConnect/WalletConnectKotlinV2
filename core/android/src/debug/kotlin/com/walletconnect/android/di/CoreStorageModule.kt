package com.walletconnect.android.di

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.walletconnect.android.internal.common.di.DatabaseConfig
import com.walletconnect.android.internal.common.di.baseStorageModule
import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import com.walletconnect.utils.Empty
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun coreStorageModule(storagePrefix: String = String.Empty) = module {

    includes(baseStorageModule(storagePrefix))

    single<SqlDriver>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE_DRIVER)) {
        AndroidSqliteDriver(
            schema = AndroidCoreDatabase.Schema,
            context = androidContext(),
            name = get<DatabaseConfig>().ANDROID_CORE_DB_NAME,
        )
    }
}

fun sdkBaseStorageModule(databaseSchema: SqlSchema<QueryResult.Value<Unit>>, databaseName: String) = module {
    single<SqlDriver>(named(databaseName)) {
        AndroidSqliteDriver(
            schema = databaseSchema,
            context = androidContext(),
            name = databaseName,
        )
    }
}