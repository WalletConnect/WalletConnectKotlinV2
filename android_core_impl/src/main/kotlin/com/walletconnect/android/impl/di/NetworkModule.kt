package com.walletconnect.android.impl.di

import com.walletconnect.android.api.RelayConnectionInterface
import com.walletconnect.android.impl.network.data.connection.ConnectivityState
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

fun networkModule(relay: RelayConnectionInterface) = module {

    single { ConnectivityState(androidApplication()) }

    single { relay }
}