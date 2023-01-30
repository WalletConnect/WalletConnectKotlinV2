@file:JvmSynthetic

package com.walletconnect.push.wallet.di

import com.walletconnect.android.di.AndroidCoreDITags
import com.walletconnect.android.di.sdkBaseStorageModule
import com.walletconnect.android.internal.common.di.DBUtils
import com.walletconnect.android.internal.common.di.deleteDatabase
import com.walletconnect.push.PushDatabase
import com.walletconnect.push.common.storage.data.SubscriptionStorageRepository
import com.walletconnect.push.common.storage.data.dao.Subscriptions
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

@JvmSynthetic
internal fun pushStorageModule(dbName: String) = module {
    fun Scope.createPushDB() = PushDatabase(
        get(),
        SubscriptionsAdapter = Subscriptions.Adapter(metadata_iconsAdapter = get(named(AndroidCoreDITags.COLUMN_ADAPTER_LIST)))
    )

    includes(sdkBaseStorageModule(PushDatabase.Schema, dbName))

    single {
        try {
            createPushDB().also {
                it.subscriptionsQueries.getAllSubscriptions().executeAsOneOrNull()
            }
        } catch (e: Exception) {
            deleteDatabase(dbName)
            createPushDB()
        }
    }

    single { get<PushDatabase>().subscriptionsQueries }

    single { SubscriptionStorageRepository(get()) }
}