package com.walletconnect.walletconnectv2.storage.history

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.util.Logger

internal class JsonRpcHistory(private val sharedPreferences: SharedPreferences) {


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
}