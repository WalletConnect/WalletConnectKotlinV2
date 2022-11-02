package com.walletconnect.android.internal.common.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.walletconnect.android.internal.common.JwtRepositoryAndroid
import com.walletconnect.android.internal.common.storage.KeyChain
import com.walletconnect.android.internal.common.storage.KeyStore
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun androidApiCryptoModule() = module {
    val keystoreAlias = "_wc_keystore_key_"
    val sharedPrefsFile = "wc_key_store"

    fun Scope.createSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(androidContext(), keystoreAlias)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            androidContext(),
            sharedPrefsFile,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun Scope.deleteSharedPreferences() {
        if (androidContext().getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE) != null) {
            androidContext().deleteSharedPreferences(sharedPrefsFile)
        }
    }

    single {
        try {
            createSharedPreferences()
        } catch (e: Exception) {
            deleteSharedPreferences()
            createSharedPreferences()
        }
    }

    single<KeyStore> { KeyChain(get()) }

    single<JwtRepository> { JwtRepositoryAndroid(get()) }
}