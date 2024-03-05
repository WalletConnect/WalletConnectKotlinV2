package com.walletconnect.web3.modal.di

import android.content.Context
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.web3.modal.domain.usecase.ConnectionEventRepository
import com.walletconnect.web3.modal.engine.Web3ModalEngine
import com.walletconnect.web3.modal.engine.coinbase.CoinbaseClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal fun engineModule() = module {

    single {
        ConnectionEventRepository(sharedPreferences = androidContext().getSharedPreferences("ConnectionEvents", Context.MODE_PRIVATE))
    }

    single {
        Web3ModalEngine(
            getSessionUseCase = get(),
            getSelectedChainUseCase = get(),
            deleteSessionDataUseCase = get(),
            saveSessionUseCase = get(),
            sendModalLoadedUseCase = get(),
            sendDisconnectErrorUseCase = get(),
            sendDisconnectSuccessUseCase = get(),
            sendConnectErrorUseCase = get(),
            sendConnectSuccessUseCase = get(),
            connectionEventRepository = get(),
            enableAnalyticsUseCase = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
        )
    }
    single {
        CoinbaseClient(
            context = get(),
            appMetaData = get()
        )
    }
}
