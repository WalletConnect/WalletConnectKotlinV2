package com.walletconnect.android.internal.common.di

import android.os.Bundle
import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun corePairingModule(pairing: PairingInterface, pairingController: PairingControllerInterface, bundleId: String) = module {
    single {
        PairingEngine(
            selfMetaData = get(),
            crypto = get(),
            metadataRepository = get(),
            pairingRepository = get(),
            jsonRpcInteractor = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            sendMalformedPairingUriUseCase = get(),
            sendPairingAlreadyExistUseCase = get(),
            sendPairingSubscriptionFailureUseCase = get(),
            sendNoInternetConnectionUseCase = get(),
            sendNoWSSConnection = get(),
            sendPairingExpiredUseCase = get(),
            eventsRepository = get()
        )
    }
    single { pairing }
    single { pairingController }
}