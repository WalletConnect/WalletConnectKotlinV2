package com.walletconnect.android.impl.di

import com.walletconnect.android.internal.common.di.androidApiCryptoModule
import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.internal.common.crypto.KeyManagementRepository
import com.walletconnect.android.impl.data.codec.ChaChaPolyCodec
import com.walletconnect.android.impl.data.repository.BouncyCastleKeyManagementRepository
import org.koin.dsl.module

fun cryptoModule() = module {

    includes(androidApiCryptoModule()) //should keep?

    single<KeyManagementRepository> { BouncyCastleKeyManagementRepository(get()) }

    single<Codec> { ChaChaPolyCodec(get()) }
}
