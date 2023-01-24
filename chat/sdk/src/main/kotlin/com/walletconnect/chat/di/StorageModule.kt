package com.walletconnect.chat.di

import com.walletconnect.android.di.sdkBaseStorageModule
import com.walletconnect.android.internal.common.di.Database
import com.walletconnect.android.internal.common.di.deleteDatabase
import com.walletconnect.chat.ChatDatabase
import com.walletconnect.chat.storage.ChatStorageRepository
import org.koin.core.scope.Scope
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule() = module {
    fun Scope.createChatDB(): ChatDatabase = ChatDatabase(get())

    includes(sdkBaseStorageModule(ChatDatabase.Schema, Database.CHAT_SDK_DB_NAME))

    single {
        try {
            createChatDB().also {
                it.contactsQueries.doesContactNotExists("").executeAsOneOrNull()
            }
        } catch (e: Exception) {
            deleteDatabase(Database.CHAT_SDK_DB_NAME)
            createChatDB()
        }
    }

    single {
        get<ChatDatabase>().contactsQueries
    }

    single {
        ChatStorageRepository(get())
    }
}