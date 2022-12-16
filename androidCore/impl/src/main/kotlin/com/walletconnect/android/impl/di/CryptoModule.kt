package com.walletconnect.android.impl.di

import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.impl.data.codec.ChaChaPolyCodec
import com.walletconnect.android.impl.data.repository.BouncyCastleKeyManagementRepository
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.wcKoinApp
import org.koin.dsl.module

fun cryptoModule() = module {

    if (wcKoinApp.koin.getOrNull<KeyManagementRepository>() == null) {
        single<KeyManagementRepository> { BouncyCastleKeyManagementRepository(get()) }
    } else {
        wcKoinApp.koin.get<KeyManagementRepository>()
    }

    if (wcKoinApp.koin.getOrNull<Codec>() == null) {
        single<Codec> { ChaChaPolyCodec(get()) }
    } else {
        wcKoinApp.koin.get<Codec>()
    }
}
