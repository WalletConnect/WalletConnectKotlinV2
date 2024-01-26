@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

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
import java.nio.charset.Charset

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

            val metadata: AppMetaData = metadataStorageRepository.getByTopicAndType(activeSubscription.topic, AppMetaDataType.PEER)
                ?: throw IllegalStateException("No metadata found for topic ${activeSubscription.topic}")

            val messageJwt = extractVerifiedDidJwtClaims<MessageRequestJwtClaim>(params.messageAuth).getOrThrow()
            messageJwt.throwIfIsInvalid(URI(metadata.url).host, activeSubscription.authenticationPublicKey.keyAsHex)

            with(messageJwt.serverNotification) {
                if (!notificationsRepository.doesNotificationsExistsByNotificationId(id)) {

                    val notification = Notification(
                        id = id, topic = request.topic.value, sentAt = sentAt, metadata = metadata,
                        notificationMessage = NotificationMessage(title = convertToUTF8(title), body = convertToUTF8(body), icon = icon, url = url, type = type),
                    )

                    notificationsRepository.insertOrReplaceNotification(notification)
                    _events.emit(notification)
                } else {
                    logger.log("OnMessageUseCase - notification already exists $id")
                }
            }

            val messageResponseJwt =
                fetchDidJwtInteractor.messageResponse(account = activeSubscription.account, app = metadata.url, authenticationKey = activeSubscription.authenticationPublicKey).getOrThrow()
            val messageResponseParams = ChatNotifyResponseAuthParams.ResponseAuth(responseAuth = messageResponseJwt.value)
            val irnParams = IrnParams(Tags.NOTIFY_MESSAGE_RESPONSE, Ttl(monthInSeconds))

            jsonRpcInteractor.respondWithParams(request.id, request.topic, messageResponseParams, irnParams, onFailure = { error -> logger.error(error) })
        } catch (e: Exception) {
            logger.error(e)
            _events.emit(SDKError(e))
        }
    }

    private fun convertToUTF8(input: String): String {
        val bytes = input.toByteArray(Charset.forName("ISO-8859-1"))
        return String(bytes, Charset.forName("UTF-8"))
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