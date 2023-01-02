package com.walletconnect.android.di

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.android.internal.common.di.DBNames
import com.walletconnect.android.internal.common.di.baseStorageModule
import com.walletconnect.android.internal.common.di.deleteDBs
import com.walletconnect.android.sdk.core.AndroidCoreDatabase
import com.walletconnect.android.impl.core.AndroidCoreDatabase
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.util.randomBytes
import net.sqlcipher.database.SupportFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEYSTORE_ALIAS = "_wc_db_key_"
private const val SHARED_PREFS_FILENAME = "db_key_store"
private const val KEY_SIZE = 256
private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
private val cipher: Cipher =
    "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}".let { transformation ->
        Cipher.getInstance(transformation)
    }
private val keyGenParameterSpec: KeyGenParameterSpec =
    KeyGenParameterSpec.Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(KEY_SIZE)
        .build()

private fun Scope.createSharedPreferences(): SharedPreferences {
    val masterKey = MasterKey.Builder(androidContext(), KEYSTORE_ALIAS)
        .setKeyGenParameterSpec(keyGenParameterSpec)
        .build()

    return EncryptedSharedPreferences.create(
        androidContext(),
        SHARED_PREFS_FILENAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

private fun Scope.deleteSharedPreferences() {
    androidContext().run {
        if (getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                deleteSharedPreferences(SHARED_PREFS_FILENAME)
            } else {
                getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
                val dir = File(applicationInfo.dataDir, "shared_prefs")
                File(dir, "$SHARED_PREFS_FILENAME.xml").delete()
            }
        }
    }
    keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
}

private fun getSecretKey(): SecretKey {
    return (keyStore.getEntry(keyGenParameterSpec.keystoreAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey ?: KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        ANDROID_KEYSTORE
    ).run {
        init(keyGenParameterSpec)
        generateKey()
    }
}

private fun signingModule() = module {

    single<ByteArray>(named(AndroidCoreDITags.DB_PASSPHRASE)) {
        val SP_ENCRYPTED_KEY = "encryptedDBKey"
        val sharedPreferences: SharedPreferences = try {
            createSharedPreferences()
        } catch (e: Exception) {
            deleteSharedPreferences()
            androidContext().databaseList().forEach { dbName -> deleteDBs(dbName) }
            createSharedPreferences()
        }
        val encryptedDBKeyFromStore: ByteArray? = sharedPreferences.getString(SP_ENCRYPTED_KEY, null)?.let { encryptedDBKey ->
            Base64.decode(encryptedDBKey, Base64.DEFAULT)
        }

        if (encryptedDBKeyFromStore == null) {
            val generatedKeyForDBByteArray = randomBytes(32)
            val secretKey: SecretKey = getSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val encryptedKey: ByteArray = cipher.doFinal(generatedKeyForDBByteArray)
            val iv: ByteArray = cipher.iv
            val ivAndEncryptedKey = ByteArray(Integer.BYTES + iv.size + encryptedKey.size)

            ByteBuffer.wrap(ivAndEncryptedKey).run {
                order(ByteOrder.BIG_ENDIAN)
                putInt(iv.size)
                put(iv)
                put(encryptedKey)
            }

            sharedPreferences.edit().putString(SP_ENCRYPTED_KEY, Base64.encodeToString(ivAndEncryptedKey, Base64.NO_WRAP)).apply()

            generatedKeyForDBByteArray
        } else {
            val buffer = ByteBuffer.wrap(encryptedDBKeyFromStore).apply {
                order(ByteOrder.BIG_ENDIAN)
            }
            val ivLength = buffer.int
            val iv = ByteArray(ivLength).apply {
                buffer.get(this)
            }
            val encryptedKey = ByteArray(encryptedDBKeyFromStore.size - Integer.BYTES - ivLength).apply {
                buffer.get(this)
            }

            val secretKey: SecretKey = getSecretKey()
            val ivSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            cipher.doFinal(encryptedKey)
        }
    }
}

fun coreStorageModule() = module {

    includes(baseStorageModule(), signingModule())

    wcKoinApp.koin.getOrNull<SqlDriver>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE_DRIVER))
        ?: single<SqlDriver>(named(AndroidCoreDITags.ANDROID_CORE_DATABASE_DRIVER)) {
            AndroidSqliteDriver(
                schema = AndroidCoreDatabase.Schema,
                context = androidContext(),
                name = DBNames.ANDROID_CORE_DB_NAME,
                factory = SupportFactory(get(named(AndroidCoreDITags.DB_PASSPHRASE)), null, false) //todo: create a separate DB_PASSHPHRASE
            )
        }
}

@SuppressLint("HardwareIds")
fun sdkBaseStorageModule(databaseSchema: SqlDriver.Schema, storageSuffix: String) = module {

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = databaseSchema,
            context = androidContext(),
            name = DBNames.getSdkDBName(storageSuffix),
            factory = SupportFactory(get(named(AndroidCoreDITags.DB_PASSPHRASE)), null, false)
        )
    }
}