package com.walletconnect.android.impl.di

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.android.impl.core.AndroidCoreDatabase
import com.walletconnect.android.internal.common.wcKoinApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun coreStorageModule() = module {

    includes(baseStorageModule())

    wcKoinApp.koin.getOrNull<SqlDriver>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE_DRIVER))
        ?: single<SqlDriver>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE_DRIVER)) {
            AndroidSqliteDriver(
                schema = AndroidCoreDatabase.Schema,
                context = androidContext(),
                name = DBNames.ANDROID_CORE_DB_NAME,
            )
        }
}

fun sdkBaseStorageModule(databaseSchema: SqlDriver.Schema, storageSuffix: String) = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = databaseSchema,
            context = androidContext(),
            name = DBNames.getSdkDBName(storageSuffix),
        )
    }
}