package com.walletconnect.web3.modal.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.web3.modal.domain.RecentWalletsRepository
import com.walletconnect.web3.modal.domain.SessionRepository
import com.walletconnect.web3.modal.domain.model.Session
import com.walletconnect.web3.modal.domain.usecase.DeleteSessionDataUseCase
import com.walletconnect.web3.modal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSessionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveRecentWalletUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveSessionUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_store")

internal fun web3ModalModule() = module {

    single { RecentWalletsRepository(sharedPreferences = get()) }

    single { GetRecentWalletUseCase(repository = get()) }
    single { SaveRecentWalletUseCase(repository = get()) }

    single<PolymorphicJsonAdapterFactory<Session>> {
        PolymorphicJsonAdapterFactory.of(Session::class.java, "type")
            .withSubtype(Session.WalletConnect::class.java, "wcsession")
            .withSubtype(Session.Coinbase::class.java, "coinbase")
    }

    single<Moshi>(named(Web3ModalDITags.MOSHI)) {
        get<Moshi.Builder>(named(AndroidCommonDITags.MOSHI))
            .add(get<PolymorphicJsonAdapterFactory<Session>>())
            .build()
    }

    single(named(Web3ModalDITags.SESSION_DATA_STORE)) { androidContext().sessionDataStore }
    single { SessionRepository(sessionStore = get(named(Web3ModalDITags.SESSION_DATA_STORE)), moshi = get(named(Web3ModalDITags.MOSHI))) }

    single { GetSessionUseCase(repository = get()) }
    single { SaveSessionUseCase(repository = get()) }
    single { DeleteSessionDataUseCase(repository = get()) }
    single { SaveChainSelectionUseCase(repository = get()) }
    single { GetSelectedChainUseCase(repository = get()) }
    single { ObserveSessionUseCase(repository = get()) }
    single { ObserveSelectedChainUseCase(repository = get()) }

    includes(blockchainApiModule(), balanceRpcModule(), engineModule())
}
