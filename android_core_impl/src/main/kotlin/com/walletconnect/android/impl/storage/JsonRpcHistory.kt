package com.walletconnect.android.impl.storage

import android.content.SharedPreferences
import com.walletconnect.android.impl.common.model.json_rpc.JsonRpcHistory
import com.walletconnect.android.impl.storage.data.dao.JsonRpcHistoryQueries
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger

class JsonRpcHistory(private val sharedPreferences: SharedPreferences, private val jsonRpcHistoryQueries: JsonRpcHistoryQueries, private val logger: Logger) {

    fun setRequest(requestId: Long, topic: Topic, method: String, payload: String): Boolean {
        return try {
            if (jsonRpcHistoryQueries.doesJsonRpcNotExist(requestId).executeAsOne()) {
                jsonRpcHistoryQueries.insertOrAbortJsonRpcHistory(requestId, topic.value, method, payload)
                jsonRpcHistoryQueries.selectLastInsertedRowId().executeAsOne() > 0L
            } else {
                logger.error("Duplicated JsonRpc RequestId: $requestId")
                false
            }
        } catch (e: Exception) {
            logger.error(e)
            false
        }
    }

    fun updateRequestWithResponse(requestId: Long, response: String): JsonRpcHistory? {
        val record = jsonRpcHistoryQueries.getJsonRpcHistoryRecord(requestId, mapper = ::mapToJsonRpc).executeAsOneOrNull()
        return if (record != null) {
            updateRecord(record, requestId, response)
        } else {
            logger.log("No JsonRpcRequest matching response")
            null
        }
    }

    private fun updateRecord(record: JsonRpcHistory, requestId: Long, response: String): JsonRpcHistory? =
        if (record.response != null) {
            logger.log("Duplicated JsonRpc RequestId: $requestId")
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

    fun getRequests(topic: Topic): List<JsonRpcHistory> =
        jsonRpcHistoryQueries.getJsonRpcRequestsDaos(topic.value, mapper = ::mapToJsonRpc).executeAsList()

    private fun mapToJsonRpc(requestId: Long, topic: String, method: String, body: String, response: String?): JsonRpcHistory =
        JsonRpcHistory(requestId, topic, method, body, response)
}