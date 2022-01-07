package com.walletconnect.walletconnectv2.storage.history

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.walletconnect.walletconnectv2.common.app
import com.walletconnect.walletconnectv2.common.model.vo.TopicVO
import com.walletconnect.walletconnectv2.util.Logger

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

    @SuppressLint("ApplySharedPref")
    fun setRequest(requestId: Long, topic: TopicVO): Boolean {
        return if (!sharedPreferences.contains(requestId.toString())) {
            sharedPreferences.edit().putString(requestId.toString(), topic.value).commit()
        } else {
            Logger.log("Duplicated JsonRpc RequestId: $requestId\tTopic: ${topic.value}")
            false
        }
    }

    fun deleteRequests(topic: TopicVO) {
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