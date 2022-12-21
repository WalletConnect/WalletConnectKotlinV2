package com.walletconnect.android.internal.common.di

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
<<<<<<<< HEAD:androidCore/sdk/src/main/kotlin/com/walletconnect/android/internal/common/di/BaseStorageModule.kt
import com.walletconnect.android.di.AndroidCoreDITags
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.*
========
import com.walletconnect.android.internal.common.di.DBNames
import com.walletconnect.android.internal.common.di.baseStorageModule
>>>>>>>> Clean up:androidCore/sdk/src/debug/kotlin/com/walletconnect/android/di/StorageModule.kt
import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

<<<<<<<< HEAD:androidCore/sdk/src/main/kotlin/com/walletconnect/android/internal/common/di/BaseStorageModule.kt
fun baseStorageModule() = module {

    fun Scope.createCoreDB(): AndroidCoreDatabase = AndroidCoreDatabase(
        get(named(AndroidCoreDITags.ANDROID_CORE_DATABASE_DRIVER)),
        MetaDataAdapter = MetaData.Adapter(
            iconsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
            typeAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_APPMETADATATYPE))
        ),
    )

    single<ColumnAdapter<List<String>, String>>(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)) {
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

    single<ColumnAdapter<AppMetaDataType, String>>(named(AndroidCoreDITags.COLUMN_ADAPTER_APPMETADATATYPE)) { EnumColumnAdapter() }

    single<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)) {
        try {
            createCoreDB().also { database -> database.jsonRpcHistoryQueries.selectLastInsertedRowId().executeAsOneOrNull() }
        } catch (e: Exception) {
            deleteDBs(DBNames.ANDROID_CORE_DB_NAME)
            createCoreDB()
        }
    }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).jsonRpcHistoryQueries }
    single { JsonRpcHistory(get(), get()) }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).pairingQueries }
    single<PairingStorageRepositoryInterface> { PairingStorageRepository(get()) }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).metaDataQueries }

    single<MetadataStorageRepositoryInterface> { MetadataStorageRepository(get()) }
========
fun coreStorageModule() = module {

    includes(baseStorageModule())
>>>>>>>> Clean up:androidCore/sdk/src/debug/kotlin/com/walletconnect/android/di/StorageModule.kt

    single<SqlDriver>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE_DRIVER)) {
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