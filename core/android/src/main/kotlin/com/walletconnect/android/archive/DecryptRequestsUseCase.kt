package com.walletconnect.android.archive

import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.ArchiveMessage
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.storage.JsonRpcHistory
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.util.Logger

internal class DecryptRequestsUseCase(
    private val chaChaPolyCodec: Codec,
    private val serializer: JsonRpcSerializer,
    private val jsonRpcHistory: JsonRpcHistory,
    private val logger: Logger,
) {

    suspend operator fun invoke(archiveMessages: List<ArchiveMessage>): Map<ClientJsonRpc, ClientParams> {
        return archiveMessages.decryptMessages()
    }

    private fun List<ArchiveMessage>.decryptMessages(): Map<ClientJsonRpc, ClientParams> = this.map { archiveMessage ->
        try {
            val topic = Topic(archiveMessage.topic)
            val decryptedMessageString = chaChaPolyCodec.decrypt(topic, archiveMessage.message)
            val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: run {
                logger.error(IllegalArgumentException("Unable to deserialize message:$decryptedMessageString"))
                return@map null
            }

            val params = deserializeRpc(clientJsonRpc, topic, decryptedMessageString)

            return@map (clientJsonRpc to params)
        } catch (e: Exception) {
            logger.error(e)
            return@map null
        }
    }.filterNotNull().toMap()

    private fun deserializeRpc(clientJsonRpc: ClientJsonRpc, topic: Topic, decryptedMessage: String): ClientParams {
        jsonRpcHistory.setRequest(clientJsonRpc.id, topic, clientJsonRpc.method, decryptedMessage)

        return serializer.deserialize(clientJsonRpc.method, decryptedMessage) ?: throw Exception("DecryptRequestsUseCase: Unknown request params - ${clientJsonRpc.method} $decryptedMessage")
    }
}