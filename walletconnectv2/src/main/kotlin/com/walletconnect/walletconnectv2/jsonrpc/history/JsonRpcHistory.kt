package com.walletconnect.walletconnectv2.jsonrpc.history

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.walletconnect.walletconnectv2.common.Topic
import com.walletconnect.walletconnectv2.util.Logger
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class JsonRpcHistory @Inject constructor(@Named("rpcStore") private val sharedPreferences: SharedPreferences) {

    @SuppressLint("ApplySharedPref")
    fun setRequest(requestId: Long, topic: Topic): Boolean {
        return if (!sharedPreferences.contains(requestId.toString())) {
            sharedPreferences.edit().putString(requestId.toString(), topic.value).commit()
        } else {
            Logger.log("Duplicated JsonRpc RequestId: $requestId\tTopic: ${topic.value}")
            false
        }
    }

    fun deleteRequests(topic: Topic) {
        sharedPreferences.all.entries
            .filter { entry -> entry.value == topic.value }
            .forEach { entry -> sharedPreferences.edit().remove(entry.key).apply() }
    }
}