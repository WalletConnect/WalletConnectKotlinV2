package com.walletconnect.wcmodal.di

import com.walletconnect.wcmodal.domain.WalletConnectModalStorage
import org.koin.dsl.module

internal fun walletConnectModalModule() = module {

    single { WalletConnectModalStorage(get()) }

}