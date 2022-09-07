package com.walletconnect.android.impl.di

import com.walletconnect.android.api.RelayConnectionInterface
import com.walletconnect.android.api.ConnectivityState
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

fun networkModule(relay: RelayConnectionInterface) = module {

    single { relay }
}