package org.walletconnect.walletconnectv2.jsonrpc.history

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.walletconnect.walletconnectv2.app
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.errors.WalletConnectExceptions

class JsonRpcHistory {
    //Region: Move to DI
    // TODO: updated based on https://stackoverflow.com/a/63357267
    private val sharedPreferences: SharedPreferences
        get() = EncryptedSharedPreferences.create(
            sharedPrefsFile,
            mainKeyAlias,
            app.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    //End of region

    @Throws(WalletConnectExceptions.DuplicatedJsonRpcException::class)
    fun setRequest(requestId: Long, topic: Topic) {
        if (sharedPreferences.contains(requestId.toString())) {
            throw WalletConnectExceptions.DuplicatedJsonRpcException("Duplicated JsonRpc")
        }
        sharedPreferences.edit().putString(requestId.toString(), topic.value).apply()
    }

    fun deleteRequests(topic: Topic) {
        sharedPreferences.all.entries
            .filter { entry -> entry.value == topic.value }
            .forEach { entry -> sharedPreferences.edit().remove(entry.key).apply() }
    }

    companion object {
        private const val sharedPrefsFile: String = "wc_rpc_store"
        private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        private val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    }
}