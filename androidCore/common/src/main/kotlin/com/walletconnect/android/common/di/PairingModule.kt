package com.walletconnect.android.common.di

import com.walletconnect.android.common.json_rpc.BaseJsonRpcInteractor
import org.koin.dsl.module

fun pairingModule() = module {

    single { BaseJsonRpcInteractor(get()) }
}