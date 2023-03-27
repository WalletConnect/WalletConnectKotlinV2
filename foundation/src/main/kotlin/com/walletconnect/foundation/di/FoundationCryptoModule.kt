@file:JvmSynthetic

package com.walletconnect.foundation.di

import com.walletconnect.foundation.common.model.PrivateKey
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.crypto.data.repository.BaseClientIdJwtRepository
import com.walletconnect.foundation.crypto.data.repository.ClientIdJwtRepository
import org.koin.dsl.module

internal fun cryptoModule() = module {

    single<ClientIdJwtRepository> {
        object: BaseClientIdJwtRepository() {
            override fun setKeyPair(key: String, privateKey: PrivateKey, publicKey: PublicKey) {}
        }
    }
}
