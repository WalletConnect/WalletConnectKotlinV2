package com.walletconnect.android.internal.common.di

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.walletconnect.android.internal.common.crypto.codec.ChaChaPolyCodec
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.kmr.BouncyCastleKeyManagementRepository
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.jwt.ClientIdJwtRepositoryAndroid
import com.walletconnect.android.internal.common.jwt.DidJwtRepository
import com.walletconnect.android.internal.common.storage.KeyChain
import com.walletconnect.foundation.crypto.data.repository.ClientIdJwtRepository
import com.walletconnect.foundation.util.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.io.File
import java.security.KeyStore
import com.walletconnect.android.internal.common.storage.KeyStore as WCKeyStore

private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val SHARED_PREFS_FILE = "wc_key_store"
private const val KEY_STORE_ALIAS = "wc_keystore_key"

@JvmSynthetic
internal fun coreCryptoModule() = module {

    @Synchronized
    fun Scope.createSharedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(androidContext(), KEY_STORE_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            androidContext(),
            SHARED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    @Synchronized
    fun deleteMasterKey() {
        KeyStore.getInstance(ANDROID_KEY_STORE).run {
            load(null)
            deleteEntry(KEY_STORE_ALIAS)
        }
    }

    @Synchronized
    fun Scope.deleteSharedPreferences() {
        try {
            androidContext().run {
                if (getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE) != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        deleteSharedPreferences(SHARED_PREFS_FILE)
                    } else {
                        getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE).edit().clear().apply()
                        val dir = File(applicationInfo.dataDir, "shared_prefs")
                        File(dir, "$SHARED_PREFS_FILE.xml").delete()
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
            deleteSharedPreferences()
            deleteMasterKey()
            deleteDatabases()
            createSharedPreferences()
        }
    }

    single<WCKeyStore> { KeyChain(get()) }

    single<ClientIdJwtRepository> { ClientIdJwtRepositoryAndroid(get()) }

    single { DidJwtRepository() }

    single<KeyManagementRepository> { BouncyCastleKeyManagementRepository(get()) }

    single<Codec> { ChaChaPolyCodec(get()) }
}