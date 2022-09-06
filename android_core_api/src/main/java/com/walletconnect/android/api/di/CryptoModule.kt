package com.walletconnect.android.api.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.walletconnect.android.api.JwtRepositoryAndroid
import com.walletconnect.android.api.KeyChain
import com.walletconnect.android.api.KeyStore
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun androidApiCryptoModule() = module {

    val sharedPrefsFile = "wc_key_store"
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    single() {
        EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<KeyStore> { KeyChain(get()) }

    single<JwtRepository> {
        println("kobe: Android JWT")

        JwtRepositoryAndroid(get())
    }
}