package com.walletconnect.auth.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.push_messages.PushMessagesRepository
import com.walletconnect.android.push.notifications.DecryptMessageUseCaseInterface
import com.walletconnect.android.utils.toClient
import com.walletconnect.auth.common.exceptions.InvalidAuthParamsType
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.foundation.common.model.Topic

class DecryptAuthMessageUseCase(
    private val codec: Codec,
    private val serializer: JsonRpcSerializer,
    private val metadataRepository: MetadataStorageRepositoryInterface,
    private val pushMessageStorageRepository: PushMessagesRepository
) : DecryptMessageUseCaseInterface {
    override suspend fun decryptNotification(topic: String, message: String, onSuccess: (Core.Model.Message) -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            if (!pushMessageStorageRepository.doesPushMessageExist(sha256(message.toByteArray()))) {
                val decryptedMessageString = codec.decrypt(Topic(topic), message)
                val clientJsonRpc: ClientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: return onFailure(InvalidAuthParamsType())
                val params: ClientParams = serializer.deserialize(clientJsonRpc.method, decryptedMessageString) ?: return onFailure(InvalidAuthParamsType())
                val metadata: AppMetaData = metadataRepository.getByTopicAndType(Topic(topic), AppMetaDataType.PEER) ?: return onFailure(InvalidAuthParamsType())

                if (params is AuthParams.RequestParams) {
                    onSuccess(params.toMessage(clientJsonRpc.id, topic, metadata))
                } else {
                    onFailure(InvalidAuthParamsType())
                }
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private fun AuthParams.RequestParams.toMessage(id: Long, topic: String, metadata: AppMetaData): Core.Model.Message.AuthRequest = with(payloadParams) {
        Core.Model.Message.AuthRequest(
            id,
            topic,
            metadata.toClient(),
            Core.Model.Message.AuthRequest.PayloadParams(type, chainId, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)
        )
    }
}