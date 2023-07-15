package com.walletconnect.android.internal.common.di

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.walletconnect.android.di.AndroidBuildVariantDITags
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.storage.*
import com.walletconnect.android.internal.common.storage.IdentitiesStorageRepository
import com.walletconnect.android.internal.common.storage.JsonRpcHistory
import com.walletconnect.android.internal.common.storage.MetadataStorageRepository
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.PairingStorageRepository
import com.walletconnect.android.internal.common.storage.PairingStorageRepositoryInterface
import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import com.walletconnect.android.sdk.storage.data.dao.MetaData
import com.walletconnect.android.sdk.storage.data.dao.VerifyContext
import com.walletconnect.android.sync.di.syncStorageModule
import com.walletconnect.utils.Empty
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun baseStorageModule(storagePrefix: String = String.Empty) = module {

    fun Scope.createCoreDB(): AndroidCoreDatabase = AndroidCoreDatabase(
        get(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE_DRIVER)),
        MetaDataAdapter = MetaData.Adapter(
            iconsAdapter = get(named(AndroidCommonDITags.COLUMN_ADAPTER_LIST)),
            typeAdapter = get(named(AndroidCommonDITags.COLUMN_ADAPTER_APPMETADATATYPE))
        ),
        VerifyContextAdapter = VerifyContext.Adapter(
            validationAdapter = get(named(AndroidCommonDITags.COLUMN_ADAPTER_VALIDATION))
        )
    )

    single<ColumnAdapter<List<String>, String>>(named(AndroidCommonDITags.COLUMN_ADAPTER_LIST)) {
        object : ColumnAdapter<List<String>, String> {
            override fun decode(databaseValue: String): List<String> =
                if (databaseValue.isBlank()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }

            override fun encode(value: List<String>): String = value.joinToString(separator = ",")
        }
    }

    single<ColumnAdapter<Map<String, String>, String>>(named(AndroidCommonDITags.COLUMN_ADAPTER_MAP)) {
        object : ColumnAdapter<Map<String, String>, String> {
            override fun decode(databaseValue: String): Map<String, String> =
                if (databaseValue.isBlank()) {
                    mapOf()
                } else {
                    databaseValue.split(",").associate { entry ->
                        val entries = entry.split("=")
                        entries.first().trim() to entries.last().trim()
                    }
                }

            override fun encode(value: Map<String, String>): String = value.entries.joinToString()
        }
    }

    single<ColumnAdapter<AppMetaDataType, String>>(named(AndroidCommonDITags.COLUMN_ADAPTER_APPMETADATATYPE)) { EnumColumnAdapter() }

    single<ColumnAdapter<Validation, String>>(named(AndroidCommonDITags.COLUMN_ADAPTER_VALIDATION)) { EnumColumnAdapter() }

    single<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)) {
        try {
            createCoreDB().also { database -> database.jsonRpcHistoryQueries.selectLastInsertedRowId().executeAsOneOrNull() }
        } catch (e: Exception) {
            deleteDatabase(get<DatabaseConfig>().ANDROID_CORE_DB_NAME)
            createCoreDB()
        }
    }

    single { get<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)).jsonRpcHistoryQueries }

    single { get<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)).pairingQueries }

    single { get<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)).metaDataQueries }

    single { get<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)).identitiesQueries }

    single { get<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)).verifyContextQueries }

    single<MetadataStorageRepositoryInterface> { MetadataStorageRepository(get()) }

    single<PairingStorageRepositoryInterface> { PairingStorageRepository(get()) }

    single { JsonRpcHistory(get(), get()) }

    single { IdentitiesStorageRepository(get()) }

    single { VerifyContextStorageRepository(get()) }

    includes(syncStorageModule())

    single { DatabaseConfig(storagePrefix) }
}