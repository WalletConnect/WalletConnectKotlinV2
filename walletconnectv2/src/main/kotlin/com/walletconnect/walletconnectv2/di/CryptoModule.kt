@file:JvmSynthetic

package com.walletconnect.walletconnectv2.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.walletconnect.walletconnectv2.crypto.CryptoRepository
import com.walletconnect.walletconnectv2.crypto.KeyStore
import com.walletconnect.walletconnectv2.crypto.data.keystore.KeyChain
import com.walletconnect.walletconnectv2.crypto.data.repository.BouncyCastleCryptoRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun cryptoManager() = module {
    val sharedPrefsFile = "wc_key_store"
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    single(named(DITags.KEYSTORE)) {
        EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<KeyStore> { KeyChain(get(named(DITags.KEYSTORE))) }

    single<CryptoRepository> { BouncyCastleCryptoRepository(get()) }
}