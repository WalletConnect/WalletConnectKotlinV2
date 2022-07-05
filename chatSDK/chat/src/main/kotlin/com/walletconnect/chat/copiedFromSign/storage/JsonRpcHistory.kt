@file:JvmSynthetic

package com.walletconnect.chat.copiedFromSign.storage

import android.content.SharedPreferences
import com.walletconnect.chat.copiedFromSign.core.model.vo.TopicVO
import com.walletconnect.chat.copiedFromSign.core.model.vo.jsonRpc.JsonRpcHistoryVO
import com.walletconnect.chat.copiedFromSign.util.Logger
import com.walletconnect.chat.storage.data.dao.rpchistory.JsonRpcHistoryQueries

internal class JsonRpcHistory(private val sharedPreferences: SharedPreferences, private val jsonRpcHistoryQueries: JsonRpcHistoryQueries) {

    fun setRequest(requestId: Long, topic: TopicVO, method: String, payload: String): Boolean {
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

    fun deleteRequests(topic: TopicVO) {
        sharedPreferences.all.entries
            .filter { entry -> entry.value == topic.value }
            .forEach { entry -> sharedPreferences.edit().remove(entry.key).apply() }

        jsonRpcHistoryQueries.deleteJsonRpcHistory(topic.value)
    }

    internal fun getRequests(topic: TopicVO): List<JsonRpcHistoryVO> =
        jsonRpcHistoryQueries.getJsonRpcRequestsDaos(topic.value, mapper = ::mapToJsonRpc).executeAsList()

    private fun mapToJsonRpc(requestId: Long, topic: String, method: String, body: String, response: String?): JsonRpcHistoryVO =
        JsonRpcHistoryVO(requestId, topic, method, body, response)
}