package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.Namespace
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.common.storage.push_messages.PushMessagesRepository
import com.walletconnect.android.push.notifications.DecryptMessageUseCaseInterface
import com.walletconnect.android.utils.toClient
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.sign.common.exceptions.InvalidSignParamsType
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams

internal class DecryptSignMessageUseCase(
    private val codec: Codec,
    private val serializer: JsonRpcSerializer,
    private val metadataRepository: MetadataStorageRepositoryInterface,
    private val pushMessageStorage: PushMessagesRepository
) : DecryptMessageUseCaseInterface {
    override suspend fun decryptNotification(topic: String, message: String, onSuccess: (Core.Model.Message) -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            if (!pushMessageStorage.doesPushMessageExist(sha256(message.toByteArray()))) {
                val decryptedMessageString = codec.decrypt(Topic(topic), message)
                val clientJsonRpc: ClientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString) ?: return onFailure(InvalidSignParamsType())
                val params: ClientParams = serializer.deserialize(clientJsonRpc.method, decryptedMessageString) ?: return onFailure(InvalidSignParamsType())
                val metadata: AppMetaData = metadataRepository.getByTopicAndType(Topic(topic), AppMetaDataType.PEER) ?: return onFailure(InvalidSignParamsType())

                when (params) {
                    is SignParams.SessionProposeParams -> onSuccess(params.toCore(clientJsonRpc.id, topic))
                    is SignParams.SessionRequestParams -> onSuccess(params.toCore(clientJsonRpc.id, topic, metadata))
                    else -> onFailure(InvalidSignParamsType())
                }
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private companion object {
        fun SignParams.SessionProposeParams.toCore(id: Long, topic: String): Core.Model.Message.SessionProposal =
            Core.Model.Message.SessionProposal(
                id,
                topic,
                proposer.metadata.name,
                proposer.metadata.description,
                proposer.metadata.url,
                proposer.metadata.icons,
                proposer.metadata.redirect?.native ?: "",
                requiredNamespaces.toCore(),
                (optionalNamespaces ?: emptyMap()).toCore(),
                properties,
                proposer.publicKey,
                relays.first().protocol,
                relays.first().data
            )

        fun SignParams.SessionRequestParams.toCore(id: Long, topic: String, metaData: AppMetaData): Core.Model.Message.SessionRequest =
            Core.Model.Message.SessionRequest(
                topic,
                chainId,
                metaData.toClient(),
                Core.Model.Message.SessionRequest.JSONRPCRequest(id, request.method, request.params)
            )

        fun Map<String, Namespace.Proposal>.toCore(): Map<String, Core.Model.Namespace.Proposal> =
            mapValues { (_, namespace) -> Core.Model.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events) }
    }
}