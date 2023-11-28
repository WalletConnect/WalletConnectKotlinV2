package com.walletconnect.auth.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.echo.notifications.DecryptMessageUseCaseInterface
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.utils.toClient
import com.walletconnect.auth.common.exceptions.InvalidAuthParamsType
import com.walletconnect.auth.common.json_rpc.AuthParams
import com.walletconnect.foundation.common.model.Topic

class DecryptAuthMessageUseCase(
    private val codec: Codec,
    private val serializer: JsonRpcSerializer,
    private val metadataRepository: MetadataStorageRepositoryInterface
) : DecryptMessageUseCaseInterface {
    override suspend fun decryptMessage(topic: String, message: String, onSuccess: (Core.Model.Message) -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            val decryptedMessageString = codec.decrypt(Topic(topic), message)
            val clientJsonRpc: ClientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: throw InvalidAuthParamsType()
            val params: ClientParams = serializer.deserialize(clientJsonRpc.method, decryptedMessageString) ?: throw InvalidAuthParamsType()
            val metadata: AppMetaData = metadataRepository.getByTopicAndType(Topic(topic), AppMetaDataType.PEER) ?: throw InvalidAuthParamsType()

            if (params is AuthParams.RequestParams) {
                onSuccess(params.toMessage(clientJsonRpc.id, topic, metadata))
            } else {
                throw InvalidAuthParamsType()
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