@file:JvmSynthetic

package com.walletconnect.notify.di

import com.squareup.sqldelight.ColumnAdapter
import com.walletconnect.android.di.sdkBaseStorageModule
import com.walletconnect.android.internal.common.di.deleteDatabase
import com.walletconnect.notify.NotifyDatabase
import com.walletconnect.notify.common.storage.data.dao.ActiveSubscriptions
import com.walletconnect.notify.data.storage.MessagesRepository
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import org.koin.core.scope.Scope
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
@JvmSynthetic
internal fun notifyStorageModule(dbName: String) = module {
    fun Scope.createNotifyDB() = NotifyDatabase(
        get(),
        ActiveSubscriptionsAdapter = ActiveSubscriptions.Adapter(
            map_of_scopeAdapter = get<ColumnAdapter<Map<String, Pair<String, Boolean>>, String>>()
        ),
    )

    includes(sdkBaseStorageModule(NotifyDatabase.Schema, dbName))

    single<ColumnAdapter<Map<String, Pair<String, Boolean>>, String>> {
        object : ColumnAdapter<Map<String, Pair<String, Boolean>>, String> {
            override fun decode(databaseValue: String): Map<String, Pair<String, Boolean>> {
                // Split string by | to get each entry
                return databaseValue.split("|").associate { entry ->
                    // Split each entry by = to get key and value
                    val entries = entry.split("=")
                    entries.first().trim() to entries.last().split(",").run {
                        // Split value by , to get description and isSubscribed
                        this.first().trim() to this.last().trim().toBoolean()
                    }
                }
            }

            override fun encode(value: Map<String, Pair<String, Boolean>>): String {
                return value.entries.joinToString(separator = "|") { entry ->
                    "${entry.key}=${entry.value.first},${entry.value.second}"
                }
            }
        }
    }

    single {
        try {
            createNotifyDB().also {
                it.activeSubscriptionsQueries.getAllActiveSubscriptions().executeAsOneOrNull()
            }
        } catch (e: Exception) {
            deleteDatabase(dbName)
            createNotifyDB()
        }
    }

    single { get<NotifyDatabase>().messagesQueries }
    single { get<NotifyDatabase>().activeSubscriptionsQueries }
    single { get<NotifyDatabase>().registeredAccountsQueries }

    single { SubscriptionRepository(activeSubscriptionsQueries = get()) }
    single { MessagesRepository(messagesQueries = get()) }
    single { RegisteredAccountsRepository(registeredAccounts = get()) }
}