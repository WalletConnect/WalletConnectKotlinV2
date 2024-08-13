package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
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
import com.walletconnect.util.generateId
import kotlinx.coroutines.supervisorScope

internal class EmitEventUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger,
) : EmitEventUseCaseInterface {

    override suspend fun emit(topic: String, event: EngineDO.Event, id: Long?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        runCatching { validate(topic, event) }.fold(
            onSuccess = {
                val eventParams = SignParams.EventParams(SessionEventVO(event.name, event.data), event.chainId)
                val sessionEvent = SignRpc.SessionEvent(id = id ?: generateId(), params = eventParams)
                val irnParams = IrnParams(Tags.SESSION_EVENT, Ttl(fiveMinutesInSeconds), true)

                logger.log("Emitting event on topic: $topic")
                jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, sessionEvent,
                    onSuccess = {
                        logger.log("Event sent successfully, on topic: $topic")
                        onSuccess()
                    },
                    onFailure = { error ->
                        logger.error("Sending event error: $error, on topic: $topic")
                        onFailure(error)
                    }
                )
            },
            onFailure = { error ->
                logger.error("Sending event error: $error, on topic: $topic")
                onFailure(error)
            }
        )
    }

    private fun validate(topic: String, event: EngineDO.Event) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            logger.error("Emit - cannot find sequence for topic: $topic")
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic))
        if (!session.isSelfController) {
            logger.error("Emit - unauthorized peer: $topic")
            throw UnauthorizedPeerException(UNAUTHORIZED_EMIT_MESSAGE)
        }

        SignValidator.validateEvent(event) { error ->
            logger.error("Emit - invalid event: $topic")
            throw InvalidEventException(error.message)
        }

        val namespaces = session.sessionNamespaces
        SignValidator.validateChainIdWithEventAuthorisation(event.chainId, event.name, namespaces) { error ->
            logger.error("Emit - unauthorized event: $topic")
            throw UnauthorizedEventException(error.message)
        }
    }
}

internal interface EmitEventUseCaseInterface {
    suspend fun emit(topic: String, event: EngineDO.Event, id: Long? = null, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}