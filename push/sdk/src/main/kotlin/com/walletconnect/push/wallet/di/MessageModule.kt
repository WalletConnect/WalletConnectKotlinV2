package com.walletconnect.push.wallet.di

import com.walletconnect.push.PushDatabase
import com.walletconnect.push.wallet.data.MessagesRepository
import org.koin.dsl.module

@JvmSynthetic
internal fun messageModule() = module {

    single {
        get<PushDatabase>().messagesQueries
    }

    single {
        MessagesRepository(get())
    }
}