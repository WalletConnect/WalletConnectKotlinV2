package com.walletconnect.android.impl.di

import com.walletconnect.android.RelayConnectionInterface
import org.koin.dsl.module

fun networkModule(relay: RelayConnectionInterface) = module {

    single { relay }
}