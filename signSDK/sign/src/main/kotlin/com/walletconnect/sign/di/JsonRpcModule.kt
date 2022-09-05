@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.sign.json_rpc.data.JsonRpcSerializer
import com.walletconnect.sign.json_rpc.domain.JsonRpcInteractor
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun jsonRpcModule() = module {

    single {
        JsonRpcSerializer(get())
    }

    single {
        println("kobe; Sign JsorRpc")

        JsonRpcInteractor(get(), get(), get(), get(), get())
    }
}