package com.walletconnect.web3.modal.di

import com.walletconnect.web3.modal.domain.RecentWalletsRepository
import com.walletconnect.web3.modal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveRecentWalletUseCase
import org.koin.dsl.module

internal fun web3ModalModule() = module {
    single { RecentWalletsRepository(get()) }

    single { GetRecentWalletUseCase(get()) }
    single { SaveRecentWalletUseCase(get()) }
}
