package com.walletconnect.chat.di

import com.walletconnect.android.impl.di.coreStorageModule
import com.walletconnect.android.impl.di.sdkBaseStorageModule
import com.walletconnect.chat.ChatDatabase
import com.walletconnect.chat.storage.ChatStorageRepository
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule(storageSuffix: String) = module {

    includes(coreStorageModule(), sdkBaseStorageModule(ChatDatabase.Schema, storageSuffix))

    single {
        ChatDatabase(get())
    }

    single {
        get<ChatDatabase>().contactsQueries
    }

    single {
        ChatStorageRepository(get())
    }
}