package com.walletconnect.android.internal.common.di

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import com.squareup.moshi.Moshi
import com.walletconnect.android.di.AndroidBuildVariantDITags
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.TransportType
import com.walletconnect.android.internal.common.model.Validation
import com.walletconnect.android.internal.common.storage.events.EventsRepository
import com.walletconnect.android.internal.common.storage.identity.IdentitiesStorageRepository
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepository
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.pairing.PairingStorageRepository
import com.walletconnect.android.internal.common.storage.pairing.PairingStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.push_messages.PushMessagesRepository
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.android.internal.common.storage.verify.VerifyContextStorageRepository
import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import com.walletconnect.android.sdk.storage.data.dao.EventDao
import com.walletconnect.android.sdk.storage.data.dao.JsonRpcHistoryDao
import com.walletconnect.android.sdk.storage.data.dao.MetaData
import com.walletconnect.android.sdk.storage.data.dao.VerifyContext
import com.walletconnect.utils.Empty
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import com.walletconnect.android.internal.common.scope as wcScope

fun baseStorageModule(storagePrefix: String = String.Empty, bundleId: String) = module {
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

    single<ColumnAdapter<TransportType, String>>(named(AndroidCommonDITags.COLUMN_ADAPTER_TRANSPORT_TYPE)) { EnumColumnAdapter() }

    single<ColumnAdapter<Validation, String>>(named(AndroidCommonDITags.COLUMN_ADAPTER_VALIDATION)) { EnumColumnAdapter() }

    fun Scope.createCoreDB(): AndroidCoreDatabase = AndroidCoreDatabase(
        driver = get(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE_DRIVER)),
        MetaDataAdapter = MetaData.Adapter(
            iconsAdapter = get(named(AndroidCommonDITags.COLUMN_ADAPTER_LIST)),
            typeAdapter = get(named(AndroidCommonDITags.COLUMN_ADAPTER_APPMETADATATYPE))
        ),
        VerifyContextAdapter = VerifyContext.Adapter(
            validationAdapter = get(named(AndroidCommonDITags.COLUMN_ADAPTER_VALIDATION))
        ),
        EventDaoAdapter = EventDao.Adapter(
            traceAdapter = get(named(AndroidCommonDITags.COLUMN_ADAPTER_LIST))
        ),
        JsonRpcHistoryDaoAdapter = JsonRpcHistoryDao.Adapter(
            transport_typeAdapter = get(named(AndroidCommonDITags.COLUMN_ADAPTER_TRANSPORT_TYPE))
        )
    )

    single<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)) {
        try {
            createCoreDB().also { database ->
                wcScope.launch {
                    try {
                        database.jsonRpcHistoryQueries.selectLastInsertedRowId().executeAsOneOrNull()
                    } catch (e: Exception) {
                        deleteDatabase(get<DatabaseConfig>().ANDROID_CORE_DB_NAME)
                        createCoreDB()
                    }
                }

            }
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

    single { get<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)).pushMessageQueries }

    single { get<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)).eventQueries }

    single { get<AndroidCoreDatabase>(named(AndroidBuildVariantDITags.ANDROID_CORE_DATABASE)).verifyPublicKeyQueries }

    single<MetadataStorageRepositoryInterface> { MetadataStorageRepository(metaDataQueries = get()) }

    single<PairingStorageRepositoryInterface> { PairingStorageRepository(pairingQueries = get()) }

    single { JsonRpcHistory(jsonRpcHistoryQueries = get(), logger = get()) }

    single { IdentitiesStorageRepository(identities = get(), get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI))) }

    single { VerifyContextStorageRepository(verifyContextQueries = get()) }

    single { PushMessagesRepository(pushMessageQueries = get()) }

    single { EventsRepository(eventQueries = get(), bundleId = bundleId, telemetryEnabled = get(named(AndroidCommonDITags.TELEMETRY_ENABLED))) }

    single { DatabaseConfig(storagePrefix = storagePrefix) }
}