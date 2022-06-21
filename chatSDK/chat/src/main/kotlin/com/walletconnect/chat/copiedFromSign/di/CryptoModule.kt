package com.walletconnect.chat.copiedFromSign.di

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.walletconnect.chat.copiedFromSign.crypto.KeyManagementRepository
import com.walletconnect.chat.copiedFromSign.crypto.KeyStore
import com.walletconnect.chat.copiedFromSign.crypto.data.keystore.KeyChain
import com.walletconnect.chat.copiedFromSign.crypto.data.repository.BouncyCastleKeyManagementRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun cryptoManager() = module {
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
}