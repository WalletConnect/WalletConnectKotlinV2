package com.walletconnect.android.internal.common.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.walletconnect.android.internal.common.JwtRepositoryAndroid
import com.walletconnect.android.internal.common.storage.KeyChain
import com.walletconnect.android.internal.common.storage.KeyStore
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun androidApiCryptoModule() = module {

    val sharedPrefsFile = "wc_key_store"

    single {
        val masterKey = MasterKey.Builder(androidContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            androidContext(),
            sharedPrefsFile,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    single<KeyStore> { KeyChain(get()) }

    single<JwtRepository> { JwtRepositoryAndroid(get()) }
}