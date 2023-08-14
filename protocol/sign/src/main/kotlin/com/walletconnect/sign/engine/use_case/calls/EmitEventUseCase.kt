package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.FIVE_MINUTES_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.InvalidEventException
import com.walletconnect.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.sign.common.exceptions.UNAUTHORIZED_EMIT_MESSAGE
import com.walletconnect.sign.common.exceptions.UnauthorizedEventException
import com.walletconnect.sign.common.exceptions.UnauthorizedPeerException
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.model.vo.clientsync.session.payload.SessionEventVO
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.storage.sequence.SessionStorageRepository

internal class EmitEventUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger
) : EmitEventUseCaseInterface {

    override fun emit(topic: String, event: EngineDO.Event, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        validate(topic, event)

        val eventParams = SignParams.EventParams(SessionEventVO(event.name, event.data), event.chainId)
        val sessionEvent = SignRpc.SessionEvent(params = eventParams)
        val irnParams = IrnParams(Tags.SESSION_EVENT, Ttl(FIVE_MINUTES_IN_SECONDS), true)

        jsonRpcInteractor.publishJsonRpcRequest(
            Topic(topic), irnParams, sessionEvent,
            onSuccess = {
                logger.log("Event sent successfully")
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending event error: $error")
                onFailure(error)
            }
        )
    }

    private fun validate(topic: String, event: EngineDO.Event) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic))
        if (!session.isSelfController) {
            throw UnauthorizedPeerException(UNAUTHORIZED_EMIT_MESSAGE)
        }

        SignValidator.validateEvent(event) { error ->
            throw InvalidEventException(error.message)
        }

        val namespaces = session.sessionNamespaces
        SignValidator.validateChainIdWithEventAuthorisation(event.chainId, event.name, namespaces) { error ->
            throw UnauthorizedEventException(error.message)
        }
    }
}

internal interface EmitEventUseCaseInterface {
    fun emit(topic: String, event: EngineDO.Event, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}