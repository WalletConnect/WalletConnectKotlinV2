package com.walletconnect.android.impl.di

import com.squareup.sqldelight.ColumnAdapter
import com.walletconnect.android.impl.Database
import com.walletconnect.android.impl.storage.JsonRpcHistory
import com.walletconnect.android.impl.storage.dao.MetadataDao
import com.walletconnect.android.impl.storage.dao.PairingDao
import com.walletconnect.android.impl.storage.PairingStorageRepository
import org.koin.dsl.module

inline fun <reified T : Database> baseStorageModule() = module {

    single<ColumnAdapter<List<String>, String>> {
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

    single { get<T>().jsonRpcHistoryQueries }
    single { get<T>().pairingQueries }
    single { get<T>().metaDataQueries }

    single { PairingDao(get()) }
    single { MetadataDao(get()) }

    single { JsonRpcHistory(get(), get()) }
    single { PairingStorageRepository(get(), get()) }
}