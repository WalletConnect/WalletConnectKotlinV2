@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

import com.walletconnect.android.echo.Message
import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.MONTH_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.jwt.decodeDidWeb
import com.walletconnect.foundation.util.jwt.decodeEd25519DidKey
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.data.jwt.message.MessageRequestJwtClaim
import com.walletconnect.notify.data.storage.MessagesRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope
import java.net.URI

internal class OnNotifyMessageUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val messagesRepository: MessagesRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: CoreNotifyParams.MessageParams) = supervisorScope {
        val activeSubscription = subscriptionRepository.getActiveSubscriptionByNotifyTopic(request.topic.value)
            ?: throw IllegalStateException("No active subscription for topic: ${request.topic.value}")

        val metadata: AppMetaData = metadataStorageRepository.getByTopicAndType(activeSubscription.notifyTopic, AppMetaDataType.PEER)
            ?: throw Exception("No metadata found for topic ${activeSubscription.notifyTopic}")


        extractVerifiedDidJwtClaims<MessageRequestJwtClaim>(params.messageAuth).onSuccess { messageJwt ->
            messageJwt.throwIfIsInvalid(URI(metadata.url).host, activeSubscription.authenticationPublicKey.keyAsHex)

            if (messagesRepository.doesMessagesExistsByRequestId(request.id)) {
                messagesRepository.updateMessageWithPublishedAtByRequestId(request.publishedAt, request.id)
            } else {
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
                    notifyMessage = Message.Notify(
                        title = messageJwt.message.title,
                        body = messageJwt.message.body,
                        icon = messageJwt.message.icon,
                        url = messageJwt.message.url,
                        type = messageJwt.message.type
                    )
                )
                _events.emit(notifyRecord)
            }
        }.runCatching {
            val messageResponseJwt = fetchDidJwtInteractor.messageResponse(
                account = activeSubscription.account,
                app = metadata.url,
                authenticationKey = activeSubscription.authenticationPublicKey,
            ).getOrThrow()

            val messageResponseParams = ChatNotifyResponseAuthParams.ResponseAuth(responseAuth = messageResponseJwt.value)
            val irnParams = IrnParams(Tags.NOTIFY_MESSAGE_RESPONSE, Ttl(MONTH_IN_SECONDS))

            jsonRpcInteractor.respondWithParams(request.id, request.topic, messageResponseParams, irnParams) { throw it }
        }.getOrElse { error ->
            _events.emit(SDKError(error))
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the notify message: ${error.message}, topic: ${request.topic}"),
                IrnParams(Tags.NOTIFY_MESSAGE_RESPONSE, Ttl(MONTH_IN_SECONDS))
            )
        }
    }

    private fun MessageRequestJwtClaim.throwIfIsInvalid(expectedApp: String, expectedIssuer: String) {
        throwIfBaseIsInvalid()
        throwIfAppIsInvalid(expectedApp)
        throwIfIssuerIsInvalid(expectedIssuer)
    }

    private fun MessageRequestJwtClaim.throwIfAppIsInvalid(expectedAppDomain: String) {
        val decodedAppDomain = decodeDidWeb(app)
        if (decodedAppDomain != expectedAppDomain) throw IllegalStateException("Invalid app claim was $decodedAppDomain instead of $expectedAppDomain")
    }


    private fun MessageRequestJwtClaim.throwIfIssuerIsInvalid(expectedIssuerAsHex: String) {
        val decodedIssuerAsHex = decodeEd25519DidKey(issuer).keyAsHex
        if (decodedIssuerAsHex != expectedIssuerAsHex) throw IllegalStateException("Invalid issuer claim was $decodedIssuerAsHex instead of $expectedIssuerAsHex")
    }
}