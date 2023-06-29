package com.walletconnect.android.internal.common.di

import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import org.koin.dsl.module

fun corePairingModule(pairing: PairingInterface, pairingController: PairingControllerInterface) = module {
    single { PairingEngine(get(), get(), get(), get(), get(), get()) }
    single { pairing }
    single { pairingController }
}