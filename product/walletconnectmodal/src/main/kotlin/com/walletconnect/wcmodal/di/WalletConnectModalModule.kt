package com.walletconnect.wcmodal.di

import com.walletconnect.wcmodal.domain.RecentWalletsRepository
import com.walletconnect.wcmodal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.wcmodal.domain.usecase.SaveRecentWalletUseCase
import org.koin.dsl.module

internal fun walletConnectModalModule() = module {

    single { RecentWalletsRepository(get()) }

    factory { GetRecentWalletUseCase(get()) }
    factory { SaveRecentWalletUseCase(get()) }
}