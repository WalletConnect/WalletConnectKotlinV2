package com.walletconnect.chat.copiedFromSign.di

import com.walletconnect.chat.copiedFromSign.crypto.Codec
import com.walletconnect.chat.copiedFromSign.crypto.data.codec.ChaChaPolyCodec
import com.walletconnect.chat.copiedFromSign.json_rpc.data.JsonRpcSerializer
import com.walletconnect.chat.copiedFromSign.json_rpc.domain.RelayerInteractor
import com.walletconnect.chat.copiedFromSign.util.NetworkState
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