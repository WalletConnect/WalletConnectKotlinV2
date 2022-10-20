package com.walletconnect.chat.di

import com.walletconnect.android.impl.di.coreStorageModule
import com.walletconnect.chat.Database
import com.walletconnect.chat.storage.ChatStorageRepository
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule(storageSuffix: String) = module {

    includes(coreStorageModule<Database>(Database.Schema, storageSuffix))

    single {
        Database(get())
    }

    single {
        get<Database>().contactsQueries
    }

    single {
        ChatStorageRepository(get())
    }
}