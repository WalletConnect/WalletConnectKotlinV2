package com.walletconnect.sign.di

import com.walletconnect.sign.util.NetworkState
import com.walletconnect.sign.relay.Codec
import com.walletconnect.sign.relay.data.codec.ChaChaPolyCodec
import com.walletconnect.sign.relay.data.serializer.JsonRpcSerializer
import com.walletconnect.sign.relay.domain.RelayerInteractor
import org.koin.dsl.module

@JvmSynthetic
internal fun relayerModule() = module {

    single<Codec> {
        ChaChaPolyCodec()
    }

    single {
        JsonRpcSerializer(get(), get(), get())
    }

    single {
        NetworkState(get())
    }

    single {
        RelayerInteractor(get(), get(), get(), get())
    }
}