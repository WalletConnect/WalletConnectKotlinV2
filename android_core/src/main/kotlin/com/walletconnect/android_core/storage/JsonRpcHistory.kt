@file:JvmSynthetic

package com.walletconnect.android_core.storage

import android.content.SharedPreferences
import com.walletconnect.android_core.common.model.vo.json_rpc.JsonRpcHistoryVO
import com.walletconnect.android_core.utils.Logger
import com.walletconnect.androidcore.storage.data.dao.JsonRpcHistoryQueries
import com.walletconnect.foundation.common.model.Topic

class JsonRpcHistory(private val sharedPreferences: SharedPreferences, private val jsonRpcHistoryQueries: JsonRpcHistoryQueries) {

    fun setRequest(requestId: Long, topic: Topic, method: String, payload: String): Boolean {
        return try {
            if (jsonRpcHistoryQueries.doesJsonRpcNotExist(requestId).executeAsOne()) {
                jsonRpcHistoryQueries.insertOrAbortJsonRpcHistory(requestId, topic.value, method, payload)
                jsonRpcHistoryQueries.selectLastInsertedRowId().executeAsOne() > 0L
            } else {
                Logger.error("Duplicated JsonRpc RequestId: $requestId")
                false
            }
        } catch (e: Exception) {
            Logger.error(e)
            false
        }
    }

    fun updateRequestWithResponse(requestId: Long, response: String): JsonRpcHistoryVO? {
        val record = jsonRpcHistoryQueries.getJsonRpcHistoryRecord(requestId, mapper = ::mapToJsonRpc).executeAsOneOrNull()
        return if (record != null) {
            updateRecord(record, requestId, response)
        } else {
            Logger.log("No JsonRpcRequest matching response")
            null
        }
    }

    private fun updateRecord(record: JsonRpcHistoryVO, requestId: Long, response: String): JsonRpcHistoryVO? =
        if (record.response != null) {
            Logger.log("Duplicated JsonRpc RequestId: $requestId")
            null
        } else {
            jsonRpcHistoryQueries.updateJsonRpcHistory(response = response, request_id = requestId)
            record
        }

    fun deleteRequests(topic: Topic) {
        sharedPreferences.all.entries
            .filter { entry -> entry.value == topic.value }
            .forEach { entry -> sharedPreferences.edit().remove(entry.key).apply() }

        jsonRpcHistoryQueries.deleteJsonRpcHistory(topic.value)
    }

    internal fun getRequests(topic: Topic): List<JsonRpcHistoryVO> =
        jsonRpcHistoryQueries.getJsonRpcRequestsDaos(topic.value, mapper = ::mapToJsonRpc).executeAsList()

    private fun mapToJsonRpc(requestId: Long, topic: String, method: String, body: String, response: String?): JsonRpcHistoryVO =
        JsonRpcHistoryVO(requestId, topic, method, body, response)
}