package com.walletconnect.push.wallet.di

import com.walletconnect.push.PushDatabase
import com.walletconnect.push.wallet.data.MessageRepository
import org.koin.dsl.module

@JvmSynthetic
internal fun messageModule() = module {

    single {
        get<PushDatabase>().messageQueries
    }

    single {
        MessageRepository(get())
    }
}