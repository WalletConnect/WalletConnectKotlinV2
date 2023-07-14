package com.walletconnect.push.dapp.di

import com.walletconnect.push.dapp.engine.PushDappEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun dappEngineModule() = module {

    single {
        PushDappEngine(
            selfAppMetaData = get(),
            jsonRpcInteractor = get(),
            pairingHandler = get(),
            extractPushConfigUseCase = get(),
            crypto = get(),
            subscriptionRepository = get(),
            castRepository = get(),
            logger = get()
        )
    }
}