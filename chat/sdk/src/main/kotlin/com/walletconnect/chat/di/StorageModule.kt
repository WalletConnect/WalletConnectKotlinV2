package com.walletconnect.chat.di

import com.walletconnect.android.di.sdkBaseStorageModule
import com.walletconnect.android.internal.common.di.DBNames
import com.walletconnect.android.internal.common.di.deleteDBs
import com.walletconnect.chat.ChatDatabase
import com.walletconnect.chat.storage.ChatStorageRepository
import org.koin.core.scope.Scope
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule(storageSuffix: String) = module {
    fun Scope.createChatDB(): ChatDatabase = ChatDatabase(get())

    includes(sdkBaseStorageModule(ChatDatabase.Schema, storageSuffix))

    single {
        try {
            createChatDB().also {
                it.contactsQueries.doesContactNotExists("").executeAsOneOrNull()
            }
        } catch (e: Exception) {
            deleteDBs(DBNames.getSdkDBName(storageSuffix))
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