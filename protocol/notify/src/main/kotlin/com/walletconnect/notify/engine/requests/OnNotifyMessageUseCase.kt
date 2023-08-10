@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

import com.squareup.moshi.Moshi
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.NotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.NotifyMessage
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.data.jwt.message.MessageRequestJwtClaim
import com.walletconnect.notify.data.storage.MessagesRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.domain.RegisterIdentityAndReturnDidJwtInteractor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnNotifyMessageUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val messagesRepository: MessagesRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val registerIdentityAndReturnDidJwt: RegisterIdentityAndReturnDidJwtInteractor,
    private val _moshi: Moshi.Builder,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: NotifyParams.MessageParams) = supervisorScope {
        extractVerifiedDidJwtClaims<MessageRequestJwtClaim>(params.messageAuth).onSuccess { messageJwt ->
            messagesRepository.insertMessage(
                requestId = request.id,
                topic = request.topic.value,
                publishedAt = request.publishedAt,
                title = messageJwt.message.title,
                body = messageJwt.message.body,
                icon = messageJwt.message.icon,
                url = messageJwt.message.url,
                type = messageJwt.message.type
            )

            val notifyRecord = NotifyRecord(
                id = request.id,
                topic = request.topic.value,
                publishedAt = request.publishedAt,
                notifyMessage = NotifyMessage(
                    title = messageJwt.message.title,
                    body = messageJwt.message.body,
                    icon = messageJwt.message.icon,
                    url = messageJwt.message.url,
                    type = messageJwt.message.type
                )
            )
            _events.emit(notifyRecord)
        }.mapCatching { jwtMessage ->
            val stringifiedMessage = _moshi.build().adapter(MessageRequestJwtClaim.Message::class.java).toJson(jwtMessage.message)
            val messageHash = sha256(stringifiedMessage.encodeToByteArray())
            val activeSubscription = subscriptionRepository.getActiveSubscriptionByNotifyTopic(request.topic.value) ?: throw IllegalStateException("No active subscription for topic: ${request.topic.value}")
            val messageReceiptJwt = registerIdentityAndReturnDidJwt.messageReceipt(
                account = activeSubscription.account,
                metadataUrl = activeSubscription.dappMetaData!!.url,
                authenticationKey = activeSubscription.authenticationPublicKey,
                messageHash = messageHash,
                onFailure = { e ->
                    throw e
                }
            ).getOrThrow()

            val messageReceiptParams = NotifyParams.MessageReceiptParams(receiptAuth = messageReceiptJwt.value)
            val irnParams = IrnParams(Tags.NOTIFY_MESSAGE_RESPONSE, Ttl(MONTH_IN_SECONDS))

            jsonRpcInteractor.respondWithParams(
                request.id,
                request.topic,
                messageReceiptParams,
                irnParams
            ) {
                throw it
            }
        }.getOrElse { e ->
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the notify message: ${e.message}, topic: ${request.topic}"),
                IrnParams(Tags.NOTIFY_MESSAGE_RESPONSE, Ttl(MONTH_IN_SECONDS))
            )
        }
    }
}