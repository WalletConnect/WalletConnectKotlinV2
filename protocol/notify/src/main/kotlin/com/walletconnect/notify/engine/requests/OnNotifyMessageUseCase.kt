@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.jwt.did.encodeDidJwt
import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.NotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.notify.common.model.NotifyMessage
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.data.jwt.message.MessageRequestJwtClaim
import com.walletconnect.notify.data.storage.MessagesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnNotifyMessageUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val messagesRepository: MessagesRepository,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: NotifyParams.MessageParams) = supervisorScope {
        val irnParams = IrnParams(Tags.NOTIFY_MESSAGE_RESPONSE, Ttl(DAY_IN_SECONDS))

        try {
            extractVerifiedDidJwtClaims<MessageRequestJwtClaim>(params.messageAuth).mapCatching { messageJwt ->
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
            }.mapCatching {

                // TODO: RespondWithParams with the receiptAuth
                //jsonRpcInteractor.respondWithParams(request.id, request.topic, )
                jsonRpcInteractor.respondWithSuccess(request, irnParams)
            }.getOrThrow()
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the notify message: ${e.message}, topic: ${request.topic}"),
                irnParams
            )
        }
    }
}