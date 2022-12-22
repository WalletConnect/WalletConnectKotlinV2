package com.walletconnect.android.impl.di

import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.impl.data.codec.ChaChaPolyCodec
import com.walletconnect.android.impl.data.repository.BouncyCastleKeyManagementRepository
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.internal.common.wcKoinApp
import org.koin.dsl.module

fun cryptoModule() = module {

    wcKoinApp.koin.getOrNull<KeyManagementRepository>() ?: single<KeyManagementRepository> { BouncyCastleKeyManagementRepository(get()) }

    wcKoinApp.koin.getOrNull<Codec>() ?: single<Codec> { ChaChaPolyCodec(get()) }

}
