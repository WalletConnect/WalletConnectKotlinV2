package com.walletconnect.android.internal.common.di

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.walletconnect.android.internal.common.crypto.codec.ChaChaPolyCodec
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.kmr.BouncyCastleKeyManagementRepository
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.jwt.clientid.ClientIdJwtRepositoryAndroid
import com.walletconnect.android.internal.common.storage.key_chain.KeyChain
import com.walletconnect.foundation.crypto.data.repository.ClientIdJwtRepository
import com.walletconnect.foundation.util.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.io.File
import java.security.KeyStore
import com.walletconnect.android.internal.common.storage.key_chain.KeyStore as WCKeyStore

private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val SHARED_PREFS_FILE = "wc_key_store"
private const val KEY_STORE_ALIAS = "wc_keystore_key"
private const val KEY_SIZE = 256
@JvmSynthetic
fun coreCryptoModule(sharedPrefsFile: String = SHARED_PREFS_FILE, keyStoreAlias: String = KEY_STORE_ALIAS) = module {

    @Synchronized
    fun Scope.createSharedPreferences(): SharedPreferences {
        val keyGenParameterSpec: KeyGenParameterSpec =
            KeyGenParameterSpec.Builder(keyStoreAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .build()

        val masterKey = MasterKey.Builder(androidContext(), keyStoreAlias)
            .setKeyGenParameterSpec(keyGenParameterSpec)
            .build()


        return EncryptedSharedPreferences.create(
            androidContext(),
            sharedPrefsFile,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    @Synchronized
    fun deleteMasterKey() {
        KeyStore.getInstance(ANDROID_KEY_STORE).run {
            load(null)
            deleteEntry(keyStoreAlias)
        }
    }

    @Synchronized
    fun Scope.deleteSharedPreferences() {
        try {
            androidContext().run {
                if (getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE) != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        deleteSharedPreferences(sharedPrefsFile)
                    } else {
                        getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE).edit().clear().apply()
                        val dir = File(applicationInfo.dataDir, "shared_prefs")
                        File(dir, "$sharedPrefsFile.xml").delete()
                    }
                }
            }
        } catch (e: Exception) {
            get<Logger>(named(AndroidCommonDITags.LOGGER)).error("Occurred when trying to reset encrypted shared prefs: $e")
        }
    }

    single {
        try {
            createSharedPreferences()
        } catch (e: Exception) {
            get<Logger>(named(AndroidCommonDITags.LOGGER)).error(e)
            deleteMasterKey()
            deleteSharedPreferences()
            deleteDatabases()
            createSharedPreferences()
        }
    }

    single<WCKeyStore> { KeyChain(get()) }

    single<ClientIdJwtRepository> { ClientIdJwtRepositoryAndroid(get()) }

    single<KeyManagementRepository> { BouncyCastleKeyManagementRepository(get()) }

    single<Codec> { ChaChaPolyCodec(get()) }
}