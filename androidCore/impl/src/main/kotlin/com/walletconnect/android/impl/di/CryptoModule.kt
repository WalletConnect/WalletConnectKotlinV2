package com.walletconnect.android.impl.di

import com.walletconnect.android.internal.common.crypto.Codec
import com.walletconnect.android.impl.data.codec.ChaChaPolyCodec
import com.walletconnect.android.impl.data.repository.BouncyCastleKeyManagementRepository
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import org.koin.dsl.module

fun cryptoModule() = module {

    single<KeyManagementRepository> { BouncyCastleKeyManagementRepository(get()) }

    single<Codec> { ChaChaPolyCodec(get()) }
}
