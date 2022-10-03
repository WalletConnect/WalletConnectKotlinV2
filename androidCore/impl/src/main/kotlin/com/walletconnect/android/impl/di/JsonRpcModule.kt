package com.walletconnect.android.impl.di

import com.walletconnect.android.common.model.JsonRpcInteractorInterface
import com.walletconnect.android.impl.json_rpc.domain.JsonRpcInteractor
import org.koin.dsl.module

@JvmSynthetic
fun jsonRpcModule() = module {

    single<JsonRpcInteractorInterface> {
        JsonRpcInteractor(get(), get(), get(), get())
    }
}