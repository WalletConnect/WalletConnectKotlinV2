@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

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
import com.walletconnect.android.internal.common.storage.metadata.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.monthInSeconds
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.foundation.util.jwt.decodeDidWeb
import com.walletconnect.foundation.util.jwt.decodeEd25519DidKey
import com.walletconnect.notify.common.model.Notification
import com.walletconnect.notify.common.model.NotificationMessage
import com.walletconnect.notify.data.jwt.message.MessageRequestJwtClaim
import com.walletconnect.notify.data.storage.NotificationsRepository
import com.walletconnect.notify.data.storage.SubscriptionRepository
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope
import java.net.URI

internal class OnMessageUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val notificationsRepository: NotificationsRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: CoreNotifyParams.MessageParams) = supervisorScope {
        try {
            val activeSubscription =
                subscriptionRepository.getActiveSubscriptionByNotifyTopic(request.topic.value) ?: throw IllegalStateException("No active subscription for topic: ${request.topic.value}")

            val metadata: AppMetaData = metadataStorageRepository.getByTopicAndType(activeSubscription.notifyTopic, AppMetaDataType.PEER)
                ?: throw IllegalStateException("No metadata found for topic ${activeSubscription.notifyTopic}")

            extractVerifiedDidJwtClaims<MessageRequestJwtClaim>(params.messageAuth)
                .onSuccess { messageJwt ->
                    messageJwt.throwIfIsInvalid(URI(metadata.url).host, activeSubscription.authenticationPublicKey.keyAsHex)


                    with(messageJwt.message) {
                        logger.log("OnMessageUseCase: $this")
                        if (!notificationsRepository.doesNotificationsExistsByNotificationId(id)) {
                            logger.log("OnMessageUseCase: $this true")

                            val notification = Notification(
                                id = id, topic = request.topic.value, sentAt = sentAt, metadata = metadata,
                                notificationMessage = NotificationMessage(title = title, body = body, icon = icon, url = url, type = type),
                            )

                            notificationsRepository.insertOrReplaceNotification(notification)
                            _events.emit(notification)
                        } else {
                            logger.log("OnMessageUseCase: $this false")

                        }
                    }
                }.runCatching {
                    val messageResponseJwt = fetchDidJwtInteractor.messageResponse(
                        account = activeSubscription.account, app = metadata.url, authenticationKey = activeSubscription.authenticationPublicKey,
                    ).getOrThrow()

                    val messageResponseParams = ChatNotifyResponseAuthParams.ResponseAuth(responseAuth = messageResponseJwt.value)
                    val irnParams = IrnParams(Tags.NOTIFY_MESSAGE_RESPONSE, Ttl(monthInSeconds))

                    jsonRpcInteractor.respondWithParams(request.id, request.topic, messageResponseParams, irnParams, onFailure = { error -> logger.error(error) })
                }.getOrElse { error ->
                    logger.error(error)
                    _events.emit(SDKError(error))
                    jsonRpcInteractor.respondWithError(
                        irnParams = IrnParams(Tags.NOTIFY_MESSAGE_RESPONSE, Ttl(monthInSeconds)), request = request,
                        error = Uncategorized.GenericError("Cannot handle the notify message: ${error.message}, topic: ${request.topic}"),
                    )
                }
        } catch (e: Exception) {
            logger.error(e)
            _events.emit(SDKError(e))
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