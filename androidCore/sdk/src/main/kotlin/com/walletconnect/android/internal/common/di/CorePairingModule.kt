package com.walletconnect.android.internal.common.di

import com.walletconnect.android.pairing.client.PairingInterface
import com.walletconnect.android.pairing.engine.domain.PairingEngine
import com.walletconnect.android.pairing.handler.PairingController
import com.walletconnect.android.pairing.handler.PairingControllerInterface
import org.koin.dsl.module

fun corePairingModule(Pairing: PairingInterface) = module {
    single { PairingEngine(get(), get(), get(), get(), get(), get()) }
    single { Pairing }
    single<PairingControllerInterface> { PairingController }
}