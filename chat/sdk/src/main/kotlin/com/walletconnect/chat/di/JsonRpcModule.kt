@file:JvmSynthetic

package com.walletconnect.chat.di

import com.walletconnect.chat.json_rpc.data.JsonRpcSerializer
import com.walletconnect.chat.json_rpc.domain.JsonRpcInteractor
import org.koin.dsl.module

@JvmSynthetic
internal fun jsonRpcModule() = module {

    single {
        JsonRpcSerializer(get())
    }

    single {
        JsonRpcInteractor(get(), get(), get(), get())
    }
}