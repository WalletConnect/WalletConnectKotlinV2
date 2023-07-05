@file:JvmSynthetic

package com.walletconnect.push.common.di

import com.squareup.sqldelight.ColumnAdapter
import com.walletconnect.android.di.sdkBaseStorageModule
import com.walletconnect.android.internal.common.di.deleteDatabase
import com.walletconnect.push.PushDatabase
import com.walletconnect.push.common.data.storage.ProposalStorageRepository
import com.walletconnect.push.common.data.storage.SubscribeStorageRepository
import com.walletconnect.push.common.storage.data.dao.ActiveSubscriptions
import com.walletconnect.push.common.storage.data.dao.RequestedSubscriptions
import org.koin.core.scope.Scope
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
@JvmSynthetic
internal fun pushStorageModule(dbName: String) = module {
    fun Scope.createPushDB() = PushDatabase(
        get(),
        ActiveSubscriptionsAdapter = ActiveSubscriptions.Adapter(
            map_of_scopeAdapter = get<ColumnAdapter<Map<String, Pair<String, Boolean>>, String>>()
        ),
        RequestedSubscriptionsAdapter = RequestedSubscriptions.Adapter(
            map_of_scopeAdapter = get<ColumnAdapter<Map<String, Pair<String, Boolean>>, String>>()
        )
    )

    includes(sdkBaseStorageModule(PushDatabase.Schema, dbName))

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
            createPushDB().also {
                it.activeSubscriptionsQueries.getAllActiveSubscriptions().executeAsOneOrNull()
            }
        } catch (e: Exception) {
            deleteDatabase(dbName)
            createPushDB()
        }
    }

    single { get<PushDatabase>().activeSubscriptionsQueries }
    single { get<PushDatabase>().requestedSubscriptionQueries }

    single { get<PushDatabase>().proposalQueries }

    single { SubscribeStorageRepository(get(), get()) }

    single { ProposalStorageRepository(get()) }
}