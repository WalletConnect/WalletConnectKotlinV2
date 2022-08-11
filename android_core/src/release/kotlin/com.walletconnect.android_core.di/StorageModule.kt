package com.walletconnect.android_core.di

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
//import com.walletconnect.sign.Database
//import com.walletconnect.sign.core.model.type.enums.MetaDataType
//import com.walletconnect.sign.storage.data.dao.metadata.MetaDataDao
//import com.walletconnect.sign.storage.data.dao.namespace.NamespaceDao
//import com.walletconnect.sign.storage.data.dao.namespace.NamespaceExtensionsDao
//import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceDao
//import com.walletconnect.sign.storage.data.dao.proposalnamespace.ProposalNamespaceExtensionsDao
//import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceDao
//import com.walletconnect.sign.storage.data.dao.temp.TempNamespaceExtensionsDao
//import com.walletconnect.sign.storage.history.JsonRpcHistory
//import com.walletconnect.sign.storage.sequence.SequenceStorageRepository
//import com.walletconnect.sign.util.randomBytes
import net.sqlcipher.database.SupportFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

@SuppressLint("HardwareIds")
fun coreStorageModule(storageSuffix: String): Module = module {

    single(named(DITags.RPC_STORE_ALIAS)) {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        MasterKeys.getOrCreate(keyGenParameterSpec)
    }

    single(named(DITags.RPC_STORE)) {
        val sharedPrefsFile = "wc_rpc_store$storageSuffix"

        EncryptedSharedPreferences.create(
            sharedPrefsFile,
            get(named(DITags.RPC_STORE_ALIAS)),
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

    single(named(DITags.DB_ALIAS)) {
        val alias = "_wc_db_key_"
        val keySize = 256
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(keySize)
            .build()

        MasterKeys.getOrCreate(keyGenParameterSpec)
    }

    single(named(DITags.DB_SECRET_KEY)) {
        fun generateSecretKey(secretKeyAlias: String): SecretKey {
            val spec = KeyGenParameterSpec
                .Builder(secretKeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()

            return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
                init(spec)
                generateKey()
            }
        }

        val alias = get<String>(named(DITags.DB_ALIAS))
        val keyStore: KeyStore = get()
        val secretKeyEntry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry

        secretKeyEntry?.secretKey ?: generateSecretKey(alias)
    }

    single(named(DITags.DB_KEY_STORAGE)) {
        val sharedPrefsFile = "db_key_store"

        EncryptedSharedPreferences.create(
            sharedPrefsFile,
            get(named(DITags.DB_ALIAS)),
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<ByteArray>(named(DITags.DB_PASSPHRASE)) {
        val SP_ENCRYPTED_KEY = "encryptedDBKey"
        val cipher: Cipher = get()
        val sharedPreferences: SharedPreferences = get(named(DITags.DB_KEY_STORAGE))
        val encryptedDBKeyFromStore: ByteArray? = sharedPreferences.getString(SP_ENCRYPTED_KEY, null)?.let { decode(it) }

        if (encryptedDBKeyFromStore == null) {
            val generatedKeyForDBByteArray = randomBytes(32)
            val secretKey: SecretKey = get(named(DITags.DB_SECRET_KEY))
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

            sharedPreferences.edit().putString(SP_ENCRYPTED_KEY, encode(ivAndEncryptedKey)).apply()

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

            val secretKey: SecretKey = get(named(DITags.DB_SECRET_KEY))
            val ivSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            cipher.doFinal(encryptedKey)
        }
    }

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = androidContext(),
            name = "WalletConnectV2$storageSuffix.db",
            factory = SupportFactory(get(named(DITags.DB_PASSPHRASE)), null, false)
        )
    }

    single {
        get<Database>().jsonRpcHistoryQueries
    }

    single {
        JsonRpcHistory(get(named(DITags.RPC_STORE)), get())
    }
}

private fun encode(decodedData: ByteArray): String = Base64.encodeToString(decodedData, Base64.NO_WRAP)
private fun decode(encodedData: String): ByteArray = Base64.decode(encodedData, Base64.DEFAULT)