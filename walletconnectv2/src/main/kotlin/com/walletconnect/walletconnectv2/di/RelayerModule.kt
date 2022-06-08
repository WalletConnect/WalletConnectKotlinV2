package com.walletconnect.walletconnectv2.di

import com.walletconnect.walletconnectv2.relay.Codec
import com.walletconnect.walletconnectv2.relay.data.codec.ChaChaPolyCodec
import com.walletconnect.walletconnectv2.relay.data.serializer.JsonRpcSerializer
import com.walletconnect.walletconnectv2.relay.domain.RelayerInteractor
import com.walletconnect.walletconnectv2.util.NetworkState
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