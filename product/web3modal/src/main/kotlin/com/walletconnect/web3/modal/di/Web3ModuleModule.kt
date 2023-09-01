package com.walletconnect.web3.modal.di

import com.walletconnect.web3.modal.domain.RecentWalletsRepository
import com.walletconnect.web3.modal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveRecentWalletUseCase
import com.walletconnect.web3.modal.domain.SessionRepository
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveSessionTopicUseCase
import org.koin.dsl.module

internal fun web3ModalModule() = module {
    single { RecentWalletsRepository(get()) }

    single { GetRecentWalletUseCase(get()) }
    single { SaveRecentWalletUseCase(get()) }

    single { SessionRepository(get()) }

    single { GetSessionTopicUseCase(get()) }
    single { SaveSessionTopicUseCase(get()) }
    single { DeleteSessionTopicUseCase(get()) }
    single { SaveChainSelectionUseCase(get()) }
    single { GetSelectedChainUseCase(get()) }
}
