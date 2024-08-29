package com.walletconnect.android.internal.common.di

import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun corePairingModule(pairing: PairingInterface, pairingController: PairingControllerInterface) = module {
    single {
        PairingEngine(
            selfMetaData = get(),
            crypto = get(),
            metadataRepository = get(),
            pairingRepository = get(),
            jsonRpcInteractor = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            insertTelemetryEventUseCase = get(),
            insertEventUseCase = get(),
            sendBatchEventUseCase = get(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            userAgent = get(named(AndroidCommonDITags.USER_AGENT))
        )
    }
    single { pairing }
    single { pairingController }
}