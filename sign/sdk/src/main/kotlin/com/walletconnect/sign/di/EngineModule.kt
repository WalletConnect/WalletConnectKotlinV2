@file:JvmSynthetic

package com.walletconnect.sign.di

import com.walletconnect.android.pairing.PairingInterface
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.mapper.toEngineAppMetaData
import com.walletconnect.sign.engine.domain.SignEngine
import com.walletconnect.sign.json_rpc.domain.GetPendingRequestsUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(metadata: Sign.Model.AppMetaData, pairingInterface: PairingInterface) = module {

    single { pairingInterface }

    //todo: Remove. Instead take Pairing selfMetaData
    single { metadata.toEngineAppMetaData() }

    //todo: check if dependencies are here
    single { GetPendingRequestsUseCase(get(), get())}

    //todo: check if dependencies are here
    single { SignEngine(get(), get(), get(), get(), get(), get(), get(), get()) }
}