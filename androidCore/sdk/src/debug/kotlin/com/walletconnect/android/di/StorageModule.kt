package com.walletconnect.android.di

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.*
import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import com.walletconnect.android.sdk.storage.data.dao.MetaData
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun coreStorageModule() = module {

//    includes(baseStorageModule())

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

    single(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)) {
        try {

            println("kobe; init core DB")

            createCoreDB().also {
                it.jsonRpcHistoryQueries.selectLastInsertedRowId().executeAsOneOrNull()
            }
        } catch (e: Exception) {

            println("kobe; re-init core DB")

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

object DBNames {
    const val ANDROID_CORE_DB_NAME = "WalletConnectAndroidCore.db"

    fun getSdkDBName(storageSuffix: String) = "WalletConnectV2$storageSuffix.db"
}

fun Scope.deleteDBs(dbName: String) {
    androidContext().deleteDatabase(dbName)
}