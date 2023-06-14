package com.walletconnect.android.internal.common.di

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.walletconnect.android.di.AndroidCoreDITags
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Redirect
import com.walletconnect.android.internal.common.storage.IdentitiesStorageRepository
import com.walletconnect.android.internal.common.storage.JsonRpcHistory
import com.walletconnect.android.internal.common.storage.MetadataStorageRepository
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.PairingStorageRepository
import com.walletconnect.android.internal.common.storage.PairingStorageRepositoryInterface
import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import com.walletconnect.android.sdk.storage.data.dao.MetaData
import com.walletconnect.android.sync.di.syncStorageModule
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

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
            override fun decode(databaseValue: String): List<String> =
                if (databaseValue.isBlank()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }

            override fun encode(value: List<String>): String = value.joinToString(separator = ",")
        }
    }

    single<ColumnAdapter<Map<String, String>, String>>(named(AndroidCoreDITags.COLUMN_ADAPTER_MAP)) {
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

    single<ColumnAdapter<AppMetaDataType, String>>(named(AndroidCoreDITags.COLUMN_ADAPTER_APPMETADATATYPE)) { EnumColumnAdapter() }

    single<ColumnAdapter<AppMetaData, String>>(named(AndroidCoreDITags.COLUMN_ADAPTER_APPMETADATA)) {
        object : ColumnAdapter<AppMetaData, String> {
            override fun decode(databaseValue: String): AppMetaData =
                if (databaseValue.isBlank()) {
                    AppMetaData("", "", "", listOf())
                } else {
                    databaseValue.split("|").associate { entry ->
                        val entries = entry.split("=")
                        entries.first().trim() to entries.last().trim()
                    }.let {
                        val redirectNative = it["redirect_native"].run { if (this.equals("null", true)) null else this }
                        val redirectUniversal = it["redirect_universal"].run { if (this.equals("null", true)) null else this }

                        val redirect = if (!redirectNative.isNullOrBlank() || !redirectUniversal.isNullOrBlank()) {
                            Redirect(redirectNative, redirectUniversal)
                        } else {
                            null
                        }

                        AppMetaData(
                            name = it["name"] ?: "",
                            description = it["description"] ?: "",
                            url = it["url"] ?: "",
                            icons = it["icons"]?.split(",") ?: listOf(),
                            redirect = redirect
                        )
                    }
                }

            override fun encode(value: AppMetaData): String = buildString {
                append("name=${value.name}")
                append("|")
                append("description=${value.description}")
                append("|")
                append("url=${value.url}")
                append("|")
                append("icons=${value.icons.joinToString("")}")
                append("|")
                append("redirect_native=${value.redirect?.native}")
                append("|")
                append("redirect_universal=${value.redirect?.universal}")
            }
        }
    }

    single<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)) {
        try {
            createCoreDB().also { database -> database.jsonRpcHistoryQueries.selectLastInsertedRowId().executeAsOneOrNull() }
        } catch (e: Exception) {
            deleteDatabase(DBUtils.ANDROID_CORE_DB_NAME)
            createCoreDB()
        }
    }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).jsonRpcHistoryQueries }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).pairingQueries }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).metaDataQueries }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).identitiesQueries }

    single<MetadataStorageRepositoryInterface> { MetadataStorageRepository(get()) }

    single<PairingStorageRepositoryInterface> { PairingStorageRepository(get()) }

    single { JsonRpcHistory(get(), get()) }

    single { IdentitiesStorageRepository(get()) }

    includes(syncStorageModule())
}