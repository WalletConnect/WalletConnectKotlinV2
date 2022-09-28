package com.walletconnect.android.common.di

import com.squareup.sqldelight.ColumnAdapter
import com.walletconnect.android.common.Database
import com.walletconnect.android.common.pairing.PairingStorageRepository
import com.walletconnect.android.common.storage.JsonRpcHistory
import com.walletconnect.android.common.storage.MetadataDao
import com.walletconnect.android.common.storage.PairingDao
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

    single {
        get<T>().jsonRpcHistoryQueries
    }

    single { get<Database>().pairingQueries }
    single { get<Database>().metaDataQueries }

    single { PairingDao(get()) }
    single { MetadataDao(get()) }

    single { PairingStorageRepository(get(), get()) }

    single { JsonRpcHistory(get(), get()) }
}