package com.walletconnect.sync.di

import com.walletconnect.android.di.sdkBaseStorageModule
import com.walletconnect.android.internal.common.di.DBUtils
import com.walletconnect.android.internal.common.di.deleteDatabase
import com.walletconnect.sync.SyncDatabase
import com.walletconnect.sync.storage.AccountsStorageRepository
import org.koin.core.scope.Scope
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule() = module {
    fun Scope.createChatDB(): SyncDatabase = SyncDatabase(
        get(),
    )

    includes(sdkBaseStorageModule(SyncDatabase.Schema, DBUtils.SYNC_SDK_DB_NAME))

    single {
        try {
            createChatDB().also {
                it.accountsQueries.doesAccountNotExists("").executeAsOneOrNull()
            }
        } catch (e: Exception) {
            deleteDatabase(DBUtils.SYNC_SDK_DB_NAME)
            createChatDB()
        }
    }

    single { get<SyncDatabase>().accountsQueries }

    single { AccountsStorageRepository(get()) }
}