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
     * 6. Retain order by retaining initial message only if is within remaining wc_syncDelete, wc_syncSet and remaining requests
     */
    suspend operator fun invoke(historyMessages: List<HistoryMessage>) {
        val decryptedMessagesSet: MutableSet<Triple<ClientJsonRpc, HistoryMessage, String>> = mutableSetOf()

        historyMessages.forEach { historyMessage ->
            try {
                val decryptedMessageString = chaChaPolyCodec.decrypt(Topic(historyMessage.topic), historyMessage.message)
                val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString)
                    ?: return@forEach logger.error(IllegalArgumentException("Unable to deserialize message:$decryptedMessageString"))

                decryptedMessagesSet.add(Triple(clientJsonRpc, historyMessage, decryptedMessageString))
            } catch (e: Exception) {
                return@forEach logger.error(e)
            }
        }

        // Temporary. Can be removed when Archive Server is Topic-centric instead of being Client-centric
        val decryptedMessagesSetWithoutDuplicates: MutableSet<Triple<ClientJsonRpc, HistoryMessage, String>> =
            decryptedMessagesSet.distinctBy { (clientJsonRpc, _, _) -> clientJsonRpc.id }.toMutableSet()
        // Temporary

        val syncDeleteParamsSet: MutableSet<Triple<ClientJsonRpc, SyncParams.DeleteParams, HistoryMessage>> = mutableSetOf()
        val syncSetParamsSet: MutableSet<Triple<ClientJsonRpc, SyncParams.SetParams, HistoryMessage>> = mutableSetOf()
        val notSyncMessagesSet: MutableSet<HistoryMessage> = mutableSetOf()

        decryptedMessagesSetWithoutDuplicates.forEach { (clientJsonRpc, historyMessage, decryptedMessageString) ->
            when (val clientParams: ClientParams? = serializer.deserialize(clientJsonRpc.method, decryptedMessageString)) {
                is SyncParams.DeleteParams -> syncDeleteParamsSet.add(Triple(clientJsonRpc, clientParams, historyMessage))
                is SyncParams.SetParams -> syncSetParamsSet.add(Triple(clientJsonRpc, clientParams, historyMessage))
                else -> notSyncMessagesSet.add(historyMessage)
            }
        }

        syncDeleteParamsSet.removeAll { (_, deleteParams, deleteHistoryMessage) ->
            syncSetParamsSet.removeAll { (_, setParams, setHistoryMessage) ->
                deleteParams.key == setParams.key && deleteHistoryMessage.topic == setHistoryMessage.topic
            }
        }

        val reducedDeleteHistoryMessages: Set<HistoryMessage> = syncDeleteParamsSet.map { (_, _, deleteHistoryMessage) -> deleteHistoryMessage }.toSet()
        val reducedSetHistoryMessages: Set<HistoryMessage> = syncSetParamsSet.map { (_, _, setHistoryMessage) -> setHistoryMessage }.toSet()

        val reducedHistoryMessages: Set<HistoryMessage> = reducedDeleteHistoryMessages + reducedSetHistoryMessages + notSyncMessagesSet

        val orderedReducedHistoryMessages: List<HistoryMessage> = historyMessages.filter { historyMessage -> historyMessage in reducedHistoryMessages }
        logger.log("Reduced fetched history message from: ${historyMessages.size} to: ${orderedReducedHistoryMessages.size}")

        orderedReducedHistoryMessages.onEach { request -> historyMessageNotifier.requestsSharedFlow.emit(request.toRelay()) }
    }
}