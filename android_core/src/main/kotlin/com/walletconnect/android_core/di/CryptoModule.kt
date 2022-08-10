package com.walletconnect.android_core.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.security.KeyStore

@JvmSynthetic
fun cryptoModule() = module {
    val sharedPrefsFile = "wc_key_store"
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    single(named(DITags.KEY_STORE)) {
        EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<KeyStore> { KeyChain(get(named(DITags.KEY_STORE))) }

    single<KeyManagementRepository> { BouncyCastleKeyManagementRepository(get()) }

    single { JwtRepository(get()) }

    single<Codec> { ChaChaPolyCodec(get()) }
}
