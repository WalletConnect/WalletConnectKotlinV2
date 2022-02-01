package com.walletconnect.walletconnectv2.storage.history

import android.content.SharedPreferences
import com.walletconnect.walletconnectv2.core.model.type.ControllerType
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.storage.history.model.JsonRpcStatus
import com.walletconnect.walletconnectv2.util.Logger
import org.walletconnect.walletconnectv2.storage.data.dao.JsonRpcHistoryQueries

internal class JsonRpcHistory(private val controllerType: ControllerType, private val sharedPreferences: SharedPreferences, private val jsonRpcHistoryQueries: JsonRpcHistoryQueries) {

    fun setRequest(requestId: Long, topic: TopicVO, method: String?, payload: String): Boolean {
        tryMigrationToDB(requestId)

        return if (jsonRpcHistoryQueries.doesJsonRpcNotExist(requestId).executeAsOne()) {
            jsonRpcHistoryQueries.insertJsonRpcHistory(requestId, topic.value, method, payload, JsonRpcStatus.PENDING, controllerType)
            jsonRpcHistoryQueries.selectLastInsertedRowId().executeAsOne() > 0L
        } else {
            Logger.log("Duplicated JsonRpc RequestId: $requestId\tTopic: ${topic.value}")
            false
        }
    }

    fun updateRequestStatus(requestId: Long, jsonRpcStatus: JsonRpcStatus) {
        jsonRpcHistoryQueries.updateJsonRpcHistory(status = jsonRpcStatus, request_id = requestId)
    }

    fun deleteRequests(topic: TopicVO) {
        sharedPreferences.all.entries
            .filter { entry -> entry.value == topic.value }
            .forEach { entry -> sharedPreferences.edit().remove(entry.key).apply() }

        jsonRpcHistoryQueries.deleteJsonRpcHistory(topic.value)
    }

    private fun tryMigrationToDB(requestId: Long) {
        if (sharedPreferences.contains(requestId.toString())) {
            sharedPreferences.getString(requestId.toString(), null)?.let { topicValue ->
                jsonRpcHistoryQueries.insertJsonRpcHistory(requestId, topicValue, null, null, JsonRpcStatus.REQUEST_SUCCESS, controllerType)
            }

            sharedPreferences.edit().remove(requestId.toString()).apply()
        }
    }
}