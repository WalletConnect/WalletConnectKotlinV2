@file:JvmSynthetic

package com.walletconnect.notify.engine.calls

import com.walletconnect.android.Core
import com.walletconnect.android.push.notifications.DecryptMessageUseCaseInterface
import com.walletconnect.android.internal.common.crypto.codec.Codec
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.json_rpc.data.JsonRpcSerializer
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.storage.rpc.JsonRpcHistory
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.notify.data.jwt.message.MessageRequestJwtClaim
import com.walletconnect.notify.data.storage.NotificationsRepository
import kotlinx.coroutines.supervisorScope
import kotlin.reflect.safeCast

internal class DecryptNotifyMessageUseCase(
    private val codec: Codec,
    private val serializer: JsonRpcSerializer,
    private val jsonRpcHistory: JsonRpcHistory,
    private val notificationsRepository: NotificationsRepository
) : DecryptMessageUseCaseInterface {

    override suspend fun decryptNotification(topic: String, message: String, onSuccess: (Core.Model.Message) -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        try {
            val decryptedMessageString = codec.decrypt(Topic(topic), message)
            val messageHash = sha256(decryptedMessageString.toByteArray())

            if (messageHash !in jsonRpcHistory.getListOfPendingRecords().map { sha256(it.body.toByteArray()) }) {
                val clientJsonRpc = serializer.tryDeserialize<ClientJsonRpc>(decryptedMessageString)
                    ?: return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match the Message format: $decryptedMessageString"))
                val notifyMessageJwt = CoreNotifyParams.MessageParams::class.safeCast(serializer.deserialize(clientJsonRpc.method, decryptedMessageString))
                    ?: return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match WalletConnect Notify Message format"))
                val messageRequestJwt = extractVerifiedDidJwtClaims<MessageRequestJwtClaim>(notifyMessageJwt.messageAuth).getOrElse {
                    return@supervisorScope onFailure(IllegalArgumentException("The decrypted message does not match WalletConnect Notify Message format"))
                }

                notificationsRepository.insertNotification(
                    requestId = clientJsonRpc.id,
                    topic = topic,
                    publishedAt = clientJsonRpc.id,
                    title = messageRequestJwt.message.title,
                    body = messageRequestJwt.message.body,
                    icon = messageRequestJwt.message.icon,
                    url = messageRequestJwt.message.url,
                    type = messageRequestJwt.message.type
                )

                onSuccess(
                    Core.Model.Message.Notify(
                        title = messageRequestJwt.message.title,
                        body = messageRequestJwt.message.body,
                        icon = messageRequestJwt.message.icon,
                        url = messageRequestJwt.message.url,
                        type = messageRequestJwt.message.type,
                        topic = topic
                    )
                )
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}