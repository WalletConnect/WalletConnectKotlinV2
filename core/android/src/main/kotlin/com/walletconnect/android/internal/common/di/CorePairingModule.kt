package com.walletconnect.android.internal.common.di

import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import org.koin.dsl.module

fun corePairingModule(pairing: PairingInterface, pairingController: PairingControllerInterface) = module {

    single {
        PairingEngine(
            logger = get(),
            selfMetaData = get(),
            metadataRepository = get(),
            crypto = get(),
            jsonRpcInteractor = get(),
            pairingRepository = get()
        )
    }

    single { pairing }
    single { pairingController }
}