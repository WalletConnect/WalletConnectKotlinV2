package com.walletconnect.walletconnectv2.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.walletconnectv2.Database
import com.walletconnect.walletconnectv2.core.model.type.enums.MetaDataType
import com.walletconnect.walletconnectv2.storage.data.dao.MetaDataDao
import com.walletconnect.walletconnectv2.storage.data.dao.NamespaceDao
import com.walletconnect.walletconnectv2.storage.data.dao.SessionDao
import com.walletconnect.walletconnectv2.storage.history.JsonRpcHistory
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStorageRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

@SuppressLint("HardwareIds")
@JvmSynthetic
internal fun storageModule(): Module = module {

    single<SharedPreferences>(named(DITags.RPC_STORE)) {
        val sharedPrefsFile = "wc_rpc_store"

        androidContext().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)
    }

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

    single<EnumColumnAdapter<MetaDataType>> {
        EnumColumnAdapter()
    }

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = androidContext(),
            name = "WalletConnectV2.db"
        )
    }

    single {
        Database(
            get(),
            SessionDaoAdapter = SessionDao.Adapter(
                accountsAdapter = get()
            ),
            MetaDataDaoAdapter = MetaDataDao.Adapter(
                iconsAdapter = get(),
                typeAdapter = get()
            ),
            NamespaceDaoAdapter = NamespaceDao.Adapter(
                chainsAdapter = get(),
                methodsAdapter = get(),
                eventsAdapter = get()
            )
        )
    }

    single {
        get<Database>().pairingDaoQueries
    }

    single {
        get<Database>().sessionDaoQueries
    }

    single {
        get<Database>().metaDataDaoQueries
    }

    single {
        get<Database>().jsonRpcHistoryQueries
    }

    single {
        SequenceStorageRepository(get(), get(), get(), get())
    }

    single {
        JsonRpcHistory(get(named(DITags.RPC_STORE)), get())
    }
}