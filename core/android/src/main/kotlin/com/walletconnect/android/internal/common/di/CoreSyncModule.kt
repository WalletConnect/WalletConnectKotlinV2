package com.walletconnect.android.internal.common.di

import com.walletconnect.android.sync.client.SyncInterface
import org.koin.dsl.module

fun coreSyncModule(Sync: SyncInterface) = module {
    single { Sync }
}