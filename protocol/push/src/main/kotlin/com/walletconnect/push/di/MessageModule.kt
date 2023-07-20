package com.walletconnect.push.di

import com.walletconnect.push.PushDatabase
import com.walletconnect.push.data.MessagesRepository
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