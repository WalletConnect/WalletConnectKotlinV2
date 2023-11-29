@file:JvmSynthetic

package com.walletconnect.notify.di

import app.cash.sqldelight.ColumnAdapter
import com.walletconnect.android.di.sdkBaseStorageModule
import com.walletconnect.android.internal.common.di.deleteDatabase
import com.walletconnect.notify.NotifyDatabase
import com.walletconnect.notify.common.storage.data.dao.ActiveSubscriptions
import com.walletconnect.notify.data.storage.MessagesRepository
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
@JvmSynthetic
internal fun notifyStorageModule(dbName: String) = module {
    fun Scope.createNotifyDB() = NotifyDatabase(
        driver = get(named(dbName)),
        ActiveSubscriptionsAdapter = ActiveSubscriptions.Adapter(
            map_of_scopeAdapter = get<ColumnAdapter<Map<String, Triple<String, String, Boolean>>, String>>()
        ),
    )

    includes(sdkBaseStorageModule(NotifyDatabase.Schema, dbName))

    single<ColumnAdapter<Map<String, Triple<String, String, Boolean>>, String>> {
        object : ColumnAdapter<Map<String, Triple<String, String, Boolean>>, String> {
            override fun decode(databaseValue: String): Map<String, Triple<String, String, Boolean>> {
                // Split string by | to get each entry
                return databaseValue.split("|").associate { entry ->
                    // Split each entry by = to get key and value
                    val entries = entry.split("=")
                    val key = entries.first().trim()
                    // Split value by , to get description and isSubscribed
                    val values = entries.last().split(",")
                    when (values.size) {
                        // Backward compatibility with example 'name=description,isSubscribed'
                        2 -> key.lowercase() to values.run {
                            Triple(key, this.first().trim(), this.last().trim().toBoolean())
                        }
                        // Current compatibility with example 'id=name,description,isSubscribed'
                        3 -> key.lowercase() to values.run {
                            Triple(this.first().trim(), this[1].trim(), this.last().trim().toBoolean())
                        }
                        // Fail-over
                        else -> key.lowercase() to Triple(key, key, false) // dummy value
                    }
                }
            }

            override fun encode(value: Map<String, Triple<String, String, Boolean>>): String {
                return value.entries.joinToString(separator = "|") { entry ->
                    "${entry.key}=${entry.value.first},${entry.value.second},${entry.value.third}"
                }
            }
        }
    }

    single {
        try {
            createNotifyDB().also {
                it.registeredAccountsQueries.getAllAccounts().executeAsList()
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