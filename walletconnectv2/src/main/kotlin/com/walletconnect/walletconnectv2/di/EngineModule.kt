package com.walletconnect.walletconnectv2.di

import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.mapper.toEngineAppMetaData
import com.walletconnect.walletconnectv2.engine.domain.EngineInteractor
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(metadata: WalletConnect.Model.AppMetaData) = module {

    single {
        metadata.toEngineAppMetaData()
    }

    single {
        EngineInteractor(get(), get(), get(), get())
    }
}