package com.walletconnect.chat.di

import com.walletconnect.chat.Database
import com.walletconnect.chat.storage.ChatStorageRepository
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule() = module {

    single {
        get<Database>().contactsQueries
    }

    single {
        ChatStorageRepository(get())
    }
}