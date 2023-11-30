package com.walletconnect.chat.di

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import com.walletconnect.android.di.sdkBaseStorageModule
import com.walletconnect.android.internal.common.di.deleteDatabase
import com.walletconnect.chat.ChatDatabase
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.common.model.InviteType
import com.walletconnect.chat.storage.*
import com.walletconnect.chat.storage.data.dao.Invites
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule(dbName: String) = module {
    @Suppress("RemoveExplicitTypeArguments")
    fun Scope.createChatDB(): ChatDatabase = ChatDatabase(
        driver = get(named(dbName)),
        InvitesAdapter = Invites.Adapter(
            statusAdapter = get<ColumnAdapter<InviteStatus, String>>(named(ChatDITags.COLUMN_ADAPTER_INVITE_STATUS)),
            typeAdapter = get<ColumnAdapter<InviteType, String>>(named(ChatDITags.COLUMN_ADAPTER_INVITE_TYPE)),
        )
    )

    includes(sdkBaseStorageModule(ChatDatabase.Schema, dbName))

    single {
        try {
            createChatDB().also {
                it.contactsQueries.doesContactNotExists("").executeAsOneOrNull()
            }
        } catch (e: Exception) {
            deleteDatabase(dbName)
            createChatDB()
        }
    }

    single<ColumnAdapter<InviteStatus, String>>(named(ChatDITags.COLUMN_ADAPTER_INVITE_STATUS)) { EnumColumnAdapter() }
    single<ColumnAdapter<InviteType, String>>(named(ChatDITags.COLUMN_ADAPTER_INVITE_TYPE)) { EnumColumnAdapter() }

    single { get<ChatDatabase>().contactsQueries }
    single { get<ChatDatabase>().threadsQueries }
    single { get<ChatDatabase>().invitesQueries }
    single { get<ChatDatabase>().messagesQueries }
    single { get<ChatDatabase>().accountsQueries }

    single { ContactStorageRepository(get()) }
    single { ThreadsStorageRepository(get()) }
    single { InvitesStorageRepository(get()) }
    single { MessageStorageRepository(get()) }
    single { AccountsStorageRepository(get()) }
}