package com.walletconnect.android_core.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.walletconnect.android_core.crypto.Codec
import com.walletconnect.android_core.crypto.KeyManagementRepository
import com.walletconnect.android_core.crypto.KeyStore
import com.walletconnect.android_core.crypto.data.codec.ChaChaPolyCodec
import com.walletconnect.android_core.crypto.data.keystore.KeyChain
import com.walletconnect.android_core.crypto.data.repository.BouncyCastleKeyManagementRepository
import com.walletconnect.android_core.crypto.data.repository.JwtRepositoryAndroid
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun cryptoModule(storageSuffix: String) = module {
    val sharedPrefsFile = "wc_key_store$storageSuffix"
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
