@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.android.pairing.PairingInterface
import com.walletconnect.sign.client.mapper.toClient
import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(pairingInterface: PairingInterface) = module {

    single { pairingInterface }

    //todo: check if dependencies are here
    single { GetPendingRequestsUseCase(get(), get())}

    //todo: check if dependencies are here
    single { SignEngine(get(), get(), get(), get(), get(), get(), get()) }
}