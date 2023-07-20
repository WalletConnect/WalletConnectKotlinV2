package com.walletconnect.android.history

import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.HistoryMessage
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.sync.common.json_rpc.SyncParams
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger

internal class ReduceSyncRequestsUseCase(
    private val historyMessageNotifier: HistoryMessageNotifier,
    private val chaChaPolyCodec: Codec,
    private val serializer: JsonRpcSerializer,
    private val logger: Logger,
) {

    /**
     * Reduction algorithm:
     * 1. Decrypt all messages, discard any exceptions
     * 2. Serialize messages to know which method it belongs to
     * 3. !Temporary! Remove messages duplicates by json rpc id
     * 4. Split them by method into sets. Method: wc_syncDelete, wc_syncSet and remaining
     * 5. Reduce any wc_syncDelete along with corresponding wc_syncSet requests
     * 6. Recreate order by retaining initial message only if is within remaining wc_syncDelete, wc_syncSet and remaining requests
     */
    suspend operator fun invoke(historyMessages: List<HistoryMessage>) {
        val decryptedMessages: List<Triple<ClientJsonRpc, HistoryMessage, String>> = historyMessages.decryptMessages()

        // Temporary. Can be removed when Archive Server is Topic-centric instead of being Client-centric
        val decryptedMessagesSetWithoutDuplicates: List<Triple<ClientJsonRpc, HistoryMessage, String>> = decryptedMessages.distinctBy { (clientJsonRpc, _, _) -> clientJsonRpc.id }
        // Temporary

        val reducedHistoryMessages = decryptedMessagesSetWithoutDuplicates.splitByType().reduceSyncSetsForAnySyncDelete()
        val orderedReducedHistoryMessages = historyMessages.recreateOrder(reducedHistoryMessages)

        logger.log("Reduced fetched history message from: ${historyMessages.size} to: ${orderedReducedHistoryMessages.size}")
        orderedReducedHistoryMessages.onEach { request -> historyMessageNotifier.requestsSharedFlow.emit(request.toRelay()) }
    }

    private fun List<HistoryMessage>.decryptMessages(): List<Triple<ClientJsonRpc, HistoryMessage, String>> = this.map { historyMessage ->
        try {
            val decryptedMessageString = chaChaPolyCodec.decrypt(Topic(historyMessage.topic), historyMessage.message)
            val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: run {
                logger.error(IllegalArgumentException("Unable to deserialize message:$decryptedMessageString"))
                return@map null
            }
            return@map Triple(clientJsonRpc, historyMessage, decryptedMessageString)
        } catch (e: Exception) {
            logger.error(e)
            return@map null
        }
    }.filterNotNull()

    private data class SplitByTypeResult(
        val syncDeleteMessages: MutableList<Triple<ClientJsonRpc, SyncParams.DeleteParams, HistoryMessage>>,
        val syncSetMessages: MutableList<Triple<ClientJsonRpc, SyncParams.SetParams, HistoryMessage>>,
        val remainingMessages: List<HistoryMessage>,
    )

    private fun List<Triple<ClientJsonRpc, HistoryMessage, String>>.splitByType(): SplitByTypeResult {
        val syncDeleteMessages: MutableList<Triple<ClientJsonRpc, SyncParams.DeleteParams, HistoryMessage>> = mutableListOf()
        val syncSetMessages: MutableList<Triple<ClientJsonRpc, SyncParams.SetParams, HistoryMessage>> = mutableListOf()
        val remainingMessages: MutableList<HistoryMessage> = mutableListOf()

        this.forEach { (clientJsonRpc, historyMessage, decryptedMessageString) ->
            when (val clientParams: ClientParams? = serializer.deserialize(clientJsonRpc.method, decryptedMessageString)) {
                is SyncParams.DeleteParams -> syncDeleteMessages.add(Triple(clientJsonRpc, clientParams, historyMessage))
                is SyncParams.SetParams -> syncSetMessages.add(Triple(clientJsonRpc, clientParams, historyMessage))
                else -> remainingMessages.add(historyMessage)
            }
        }
        return SplitByTypeResult(syncDeleteMessages, syncSetMessages, remainingMessages)
    }

    private fun SplitByTypeResult.reduceSyncSetsForAnySyncDelete(): List<HistoryMessage> {
        syncDeleteMessages.removeAll { (_, deleteParams, deleteHistoryMessage) ->
            syncSetMessages.removeAll { (_, setParams, setHistoryMessage) ->
                deleteParams.key == setParams.key && deleteHistoryMessage.topic == setHistoryMessage.topic
            }
        }
        val reducedDeleteHistoryMessages: List<HistoryMessage> = syncDeleteMessages.map { (_, _, deleteHistoryMessage) -> deleteHistoryMessage }
        val reducedSetHistoryMessages: List<HistoryMessage> = syncSetMessages.map { (_, _, setHistoryMessage) -> setHistoryMessage }
        return reducedDeleteHistoryMessages + reducedSetHistoryMessages + remainingMessages
    }

    private fun List<HistoryMessage>.recreateOrder(reducedHistoryMessages: List<HistoryMessage>): List<HistoryMessage> = filter { historyMessage -> historyMessage in reducedHistoryMessages }
}