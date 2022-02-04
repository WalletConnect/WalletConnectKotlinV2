package com.walletconnect.walletconnectv2.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.walletconnectv2.Database
import com.walletconnect.walletconnectv2.storage.history.JsonRpcHistory
import com.walletconnect.walletconnectv2.storage.sequence.SequenceStorageRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.walletconnect.walletconnectv2.storage.data.dao.JsonRpcHistoryDao
import com.walletconnect.walletconnectv2.storage.data.dao.MetaDataDao
import com.walletconnect.walletconnectv2.storage.data.dao.PairingDao
import com.walletconnect.walletconnectv2.storage.data.dao.SessionDao

@JvmSynthetic
internal fun storageModule() = module {
    val sharedPrefsFile: String = "wc_rpc_store"
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    single(named(DITags.RPC_STORE)) {
        EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<ColumnAdapter<List<String>, String>> {
        object : ColumnAdapter<List<String>, String> {

            override fun decode(databaseValue: String) =
                if (databaseValue.isEmpty()) {
                    listOf()
                } else {
                    databaseValue.split(",")
                }

            override fun encode(value: List<String>) = value.joinToString(separator = ",")
        }
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
            PairingDaoAdapter = PairingDao.Adapter(
                statusAdapter = EnumColumnAdapter(),
                controller_typeAdapter = EnumColumnAdapter(),
                permissionsAdapter = get()
            ),
            SessionDaoAdapter = SessionDao.Adapter(
                permissions_chainsAdapter = get(),
                permissions_methodsAdapter = get(),
                permissions_typesAdapter = get(),
                accountsAdapter = get(),
                statusAdapter = EnumColumnAdapter(),
                controller_typeAdapter = EnumColumnAdapter()
            ),
            MetaDataDaoAdapter = MetaDataDao.Adapter(iconsAdapter = get()),
            JsonRpcHistoryDaoAdapter = JsonRpcHistoryDao.Adapter(
                statusAdapter = EnumColumnAdapter(),
                controller_typeAdapter = EnumColumnAdapter()
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
        SequenceStorageRepository(get(), get(), get())
    }

    single {
        JsonRpcHistory(get(), get(named(DITags.RPC_STORE)), get())
    }
}