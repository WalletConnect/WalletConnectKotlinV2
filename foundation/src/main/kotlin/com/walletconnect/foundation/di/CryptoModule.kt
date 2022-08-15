package com.walletconnect.foundation.di

import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.crypto.data.repository.BaseJwtRepository
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import org.koin.dsl.module

internal fun cryptoModule() = module {

    single<JwtRepository> {
        object: BaseJwtRepository() {
            override fun setKeyPair(key: String, privateKey: PrivateKey, publicKey: PublicKey) {}
        }
    }
}
