package com.walletconnect.web3.modal.di

import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.web3.modal.domain.RecentWalletsRepository
import com.walletconnect.web3.modal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveRecentWalletUseCase
import com.walletconnect.web3.modal.domain.SessionRepository
import com.walletconnect.web3.modal.domain.model.buildWeb3ModalMoshi
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveSessionTopicUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal fun web3ModalModule() = module {

    single { RecentWalletsRepository(sharedPreferences = get()) }

    single { GetRecentWalletUseCase(repository = get()) }
    single { SaveRecentWalletUseCase(repository = get()) }

    single(named(Web3ModalDITags.MOSHI)) { buildWeb3ModalMoshi(get(named(AndroidCommonDITags.MOSHI))) }
    single { SessionRepository(context = androidContext(), moshi = get(named(Web3ModalDITags.MOSHI))) }

    single { GetSessionTopicUseCase(repository = get()) }
    single { SaveSessionTopicUseCase(repository = get()) }
    single { DeleteSessionDataUseCase(repository = get()) }
    single { DeleteSessionTopicUseCase(repository = get()) }
    single { SaveChainSelectionUseCase(repository = get()) }
    single { GetSelectedChainUseCase(repository = get()) }
    single { ObserveSessionTopicUseCase(repository = get()) }
    single { ObserveSelectedChainUseCase(repository = get()) }

    includes(blockchainApiModule(), balanceRpcModule())
}
