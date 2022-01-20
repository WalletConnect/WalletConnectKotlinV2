package com.walletconnect.walletconnectv2.di

import com.walletconnect.walletconnectv2.relay.Codec
import com.walletconnect.walletconnectv2.relay.data.codec.AuthenticatedEncryptionCodec
import com.walletconnect.walletconnectv2.relay.data.serializer.JsonRpcSerializer
import com.walletconnect.walletconnectv2.relay.domain.WalletConnectRelayer
import org.koin.dsl.module

@JvmSynthetic
internal fun relayerModule() = module {

    single<Codec> {
        AuthenticatedEncryptionCodec()
    }

    single {
        JsonRpcSerializer(get(), get(), get())
    }

    single {
        WalletConnectRelayer(get(), get(), get())
    }
}