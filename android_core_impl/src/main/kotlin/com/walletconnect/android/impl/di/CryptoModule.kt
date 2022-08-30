package com.walletconnect.android.impl.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.walletconnect.android.impl.crypto.Codec
import com.walletconnect.android.impl.crypto.KeyManagementRepository
import com.walletconnect.android.impl.crypto.KeyStore
import com.walletconnect.android.impl.data.codec.ChaChaPolyCodec
import com.walletconnect.android.impl.data.keystore.KeyChain
import com.walletconnect.android.impl.data.repository.BouncyCastleKeyManagementRepository
import com.walletconnect.android.impl.data.repository.JwtRepositoryAndroid
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun cryptoModule() = module {
    val sharedPrefsFile = "wc_key_store"
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    single(named(AndroidCoreDITags.KEY_STORE)) {
        EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<KeyStore> { KeyChain(get(named(AndroidCoreDITags.KEY_STORE))) }

    single<KeyManagementRepository> { BouncyCastleKeyManagementRepository(get()) }

    single<JwtRepository> { JwtRepositoryAndroid(get()) }

    single<Codec> { ChaChaPolyCodec(get()) }
}
