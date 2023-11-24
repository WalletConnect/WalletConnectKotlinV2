package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.echo.DecryptMessageUseCaseInterface
import com.walletconnect.android.echo.Message
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams

internal class DecryptSignMessageUseCase(
    private val codec: Codec,
    private val serializer: JsonRpcSerializer,
    private val metadataRepository: MetadataStorageRepositoryInterface
) : DecryptMessageUseCaseInterface {
    override suspend fun decryptMessage(topic: String, message: String, onSuccess: (Message) -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            val decryptedMessageString = codec.decrypt(Topic(topic), message)

            println("kobe:Message SIGN: $decryptedMessageString")

            val clientJsonRpc: ClientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: return onFailure(Throwable("Error"))
            val params: ClientParams = serializer.deserialize(clientJsonRpc.method, decryptedMessageString) ?: return onFailure(Throwable("Error"))
            val metadata: AppMetaData = metadataRepository.getByTopicAndType(Topic(topic), AppMetaDataType.PEER) ?: return onFailure(Throwable("Error"))

            when (params) {
                is SignParams.SessionProposeParams -> onSuccess(params.toMessage(clientJsonRpc.id, topic))
                is SignParams.SessionRequestParams -> onSuccess(params.toMessage(clientJsonRpc.id, topic, metadata))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private fun SignParams.SessionProposeParams.toMessage(id: Long, topic: String): Message.SessionProposal =
        Message.SessionProposal(
            id,
            topic,
            proposer.metadata.name,
            proposer.metadata.description,
            proposer.metadata.url,
            proposer.metadata.icons,
            proposer.metadata.redirect?.native ?: "",
            requiredNamespaces,
            optionalNamespaces ?: emptyMap(),
            properties,
            proposer.publicKey,
            relays.first().protocol,
            relays.first().data
        )

    private fun SignParams.SessionRequestParams.toMessage(id: Long, topic: String, metaData: AppMetaData): Message.SessionRequest =
        Message.SessionRequest(
            topic,
            chainId,
            metaData,
            Message.SessionRequest.JSONRPCRequest(id, request.method, request.params)
        )
}