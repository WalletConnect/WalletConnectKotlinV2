package com.walletconnect.android.impl.di

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.walletconnect.android.impl.core.AndroidCoreDatabase
import com.walletconnect.android.impl.storage.JsonRpcHistory
import com.walletconnect.android.impl.storage.MetadataStorageRepository
import com.walletconnect.android.impl.storage.PairingStorageRepository
import com.walletconnect.android.impl.storage.data.dao.MetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.PairingStorageRepositoryInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun baseStorageModule() = module {

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
        AndroidCoreDatabase(
            get(named(AndroidCoreDITags.ANDROID_CORE_DATABASE_DRIVER)),
            MetaDataAdapter = MetaData.Adapter(
                iconsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)),
                typeAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_APPMETADATATYPE))
            ),
        )
    }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).jsonRpcHistoryQueries }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).pairingQueries }

    single { get<AndroidCoreDatabase>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE)).metaDataQueries }

    single<MetadataStorageRepositoryInterface> { MetadataStorageRepository(get()) }

    single<PairingStorageRepositoryInterface> { PairingStorageRepository(get()) }

    single { JsonRpcHistory(get(), get()) }
}