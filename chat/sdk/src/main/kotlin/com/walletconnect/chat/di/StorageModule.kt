package com.walletconnect.chat.di

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.walletconnect.android.di.sdkBaseStorageModule
import com.walletconnect.android.internal.common.di.DBUtils
import com.walletconnect.android.internal.common.di.deleteDatabase
import com.walletconnect.android.internal.common.storage.IdentitiesStorageRepository
import com.walletconnect.chat.ChatDatabase
import com.walletconnect.chat.common.model.InviteStatus
import com.walletconnect.chat.common.model.InviteType
import com.walletconnect.chat.storage.*
import com.walletconnect.chat.storage.data.dao.Invites
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

@JvmSynthetic
internal fun storageModule() = module {
    fun Scope.createChatDB(): ChatDatabase = ChatDatabase(
        get(), InvitesAdapter = Invites.Adapter(
            statusAdapter = get(named(ChatDITags.COLUMN_ADAPTER_INVITE_STATUS)),
            typeAdapter = get(named(ChatDITags.COLUMN_ADAPTER_INVITE_TYPE)),
        )
    )

    includes(sdkBaseStorageModule(ChatDatabase.Schema, DBUtils.CHAT_SDK_DB_NAME))

    single {
        try {
            createChatDB().also {
                it.contactsQueries.doesContactNotExists("").executeAsOneOrNull()
            }
        } catch (e: Exception) {
            deleteDatabase(DBUtils.CHAT_SDK_DB_NAME)
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