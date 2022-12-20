package com.walletconnect.android.internal.common.di

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.walletconnect.android.internal.common.JwtRepositoryAndroid
import com.walletconnect.android.internal.common.crypto.codec.ChaChaPolyCodec
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.kmr.BouncyCastleKeyManagementRepository
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.storage.KeyChain
import com.walletconnect.android.internal.common.storage.KeyStore
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import com.walletconnect.foundation.util.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.io.File

fun androidApiCryptoModule() = module {
    val keystoreAlias = "wc_keystore_key"
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
        androidContext().run {
            if (getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE) != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    deleteSharedPreferences(sharedPrefsFile)
                } else {
                    getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
                    val dir = File(applicationInfo.dataDir, "shared_prefs")
                    File(dir, "$sharedPrefsFile.xml").delete()
                }
            }
        }
    }

    single {
        try {
            createSharedPreferences()
        } catch (e: Exception) {
            get<Logger>(named(AndroidCommonDITags.LOGGER)).error(e)
            deleteSharedPreferences()
            createSharedPreferences()
        }
    }

    single<KeyStore> { KeyChain(get()) }

    single<JwtRepository> { JwtRepositoryAndroid(get()) }

    single<KeyManagementRepository> { BouncyCastleKeyManagementRepository(get()) }

    single<Codec> { ChaChaPolyCodec(get()) }
}