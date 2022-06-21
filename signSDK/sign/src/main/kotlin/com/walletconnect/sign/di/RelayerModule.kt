package com.walletconnect.sign.di

import com.walletconnect.sign.crypto.Codec
import com.walletconnect.sign.crypto.data.codec.ChaChaPolyCodec
import com.walletconnect.sign.relay.data.JsonRpcSerializer
import com.walletconnect.sign.relay.domain.RelayerInteractor
import com.walletconnect.sign.util.NetworkState
import org.koin.dsl.module

@JvmSynthetic
internal fun relayerModule() = module {

    single<Codec> {
        ChaChaPolyCodec(get())
    }

    single {
        JsonRpcSerializer(get())
    }

    single {
        NetworkState(get())
    }

    single {
        RelayerInteractor(get(), get(), get(), get(), get())
    }
}