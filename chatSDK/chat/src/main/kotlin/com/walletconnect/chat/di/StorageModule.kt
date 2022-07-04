package com.walletconnect.chat.di

import com.walletconnect.chat.Database
import com.walletconnect.chat.storage.ChatStorageRepository
import com.walletconnect.chat.storage.ContactsDao
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule() = module {

    single {
        get<Database>().contactsQueries
    }

    single {
        ContactsDao(get())
    }

    single {
        ChatStorageRepository(get())
    }
}