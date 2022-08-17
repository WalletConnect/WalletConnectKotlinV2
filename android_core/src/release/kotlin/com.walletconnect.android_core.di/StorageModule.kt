package com.walletconnect.android_core.di

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.walletconnect.android_core.Database
import com.walletconnect.android_core.storage.JsonRpcHistory
import com.walletconnect.util.randomBytes
import net.sqlcipher.database.SupportFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@SuppressLint("HardwareIds")
inline fun <reified T : Database> coreStorageModule(databaseSchema: SqlDriver.Schema, storageSuffix: String) = module {

    single(named(AndroidCoreDITags.RPC_STORE_ALIAS)) {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        MasterKeys.getOrCreate(keyGenParameterSpec)
    }

    single(named(AndroidCoreDITags.RPC_STORE)) {
        val sharedPrefsFile = "wc_rpc_store$storageSuffix"

        EncryptedSharedPreferences.create(
            sharedPrefsFile,
            get(named(AndroidCoreDITags.RPC_STORE_ALIAS)),
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<KeyStore> {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }

    single<Cipher> {
        val TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"

        Cipher.getInstance(TRANSFORMATION)
    }

    single(named(AndroidCoreDITags.DB_ALIAS)) {
        val alias = "_wc_db_key_"
        val keySize = 256
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(keySize)
            .build()

        MasterKeys.getOrCreate(keyGenParameterSpec)
    }

    single(named(AndroidCoreDITags.DB_SECRET_KEY)) {
        val alias = get<String>(named(AndroidCoreDITags.DB_ALIAS))
        val keyStore: KeyStore = get()
        val secretKeyEntry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry

        secretKeyEntry?.secretKey ?: generateSecretKey(alias)
    }

    single(named(AndroidCoreDITags.DB_KEY_STORAGE)) {
        val sharedPrefsFile = "db_key_store"

        EncryptedSharedPreferences.create(
            sharedPrefsFile,
            get(named(AndroidCoreDITags.DB_ALIAS)),
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<ByteArray>(named(AndroidCoreDITags.DB_PASSPHRASE)) {
        val SP_ENCRYPTED_KEY = "encryptedDBKey"
        val cipher: Cipher = get()
        val sharedPreferences: SharedPreferences = get(named(AndroidCoreDITags.DB_KEY_STORAGE))
        val encryptedDBKeyFromStore: ByteArray? = sharedPreferences.getString(SP_ENCRYPTED_KEY, null)?.let { Base64.decode(it, Base64.DEFAULT) }

        if (encryptedDBKeyFromStore == null) {
            val generatedKeyForDBByteArray = randomBytes(32)
            val secretKey: SecretKey = get(named(AndroidCoreDITags.DB_SECRET_KEY))
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

            val secretKey: SecretKey = get(named(AndroidCoreDITags.DB_SECRET_KEY))
            val ivSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            cipher.doFinal(encryptedKey)
        }
    }

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = databaseSchema,
            context = androidContext(),
            name = "WalletConnectV2$storageSuffix.db",
            factory = SupportFactory(get(named(AndroidCoreDITags.DB_PASSPHRASE)), null, false)
        )
    }

    single {
        get<Database>().jsonRpcHistoryQueries
    }

    single {
        JsonRpcHistory(get(named(AndroidCoreDITags.RPC_STORE)), get(), get())
    }
}