@file:JvmSynthetic

package com.walletconnect.walletconnectv2.storage.history

import android.content.SharedPreferences
import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType
import com.walletconnect.walletconnectv2.core.model.vo.TopicVO
import com.walletconnect.walletconnectv2.core.model.vo.jsonRpc.JsonRpcHistoryVO
import com.walletconnect.walletconnectv2.storage.data.dao.JsonRpcHistoryQueries
import com.walletconnect.walletconnectv2.util.Logger

internal class JsonRpcHistory(
    private val controllerType: ControllerType,
    private val sharedPreferences: SharedPreferences,
    private val jsonRpcHistoryQueries: JsonRpcHistoryQueries
) {

    fun setRequest(requestId: Long, topic: TopicVO, method: String, payload: String): Boolean {
        return if (jsonRpcHistoryQueries.doesJsonRpcNotExist(requestId).executeAsOne()) {
            jsonRpcHistoryQueries.insertJsonRpcHistory(requestId, topic.value, method, payload, controllerType)
            jsonRpcHistoryQueries.selectLastInsertedRowId().executeAsOne() > 0L
        } else {
            Logger.log("Duplicated JsonRpc RequestId: $requestId")
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

    private fun mapToJsonRpc(
        requestId: Long,
        topic: String,
        method: String,
        body: String,
        response: String?,
        controllerType: ControllerType
    ): JsonRpcHistoryVO =
        JsonRpcHistoryVO(requestId, topic, method, body, response, controllerType)
}