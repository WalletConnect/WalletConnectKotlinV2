package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.GenericException
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.InvalidNamespaceException
import com.walletconnect.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.sign.common.exceptions.NotSettledSessionException
import com.walletconnect.sign.common.exceptions.SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE
import com.walletconnect.sign.common.exceptions.UNAUTHORIZED_UPDATE_MESSAGE
import com.walletconnect.sign.common.exceptions.UnauthorizedPeerException
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toMapOfNamespacesVOSession
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.supervisorScope

internal class SessionUpdateUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger,
) : SessionUpdateUseCaseInterface {

    override suspend fun sessionUpdate(
        topic: String,
        namespaces: Map<String, EngineDO.Namespace.Session>,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = supervisorScope {
        runCatching { validate(topic, namespaces) }.fold(
            onSuccess = {
                val params = SignParams.UpdateNamespacesParams(namespaces.toMapOfNamespacesVOSession())
                val sessionUpdate = SignRpc.SessionUpdate(params = params)
                val irnParams = IrnParams(Tags.SESSION_UPDATE, Ttl(dayInSeconds))

                try {
                    logger.log("Sending session update on topic: $topic")
                    sessionStorageRepository.insertTempNamespaces(topic, namespaces.toMapOfNamespacesVOSession(), sessionUpdate.id)
                    jsonRpcInteractor.publishJsonRpcRequest(
                        Topic(topic), irnParams, sessionUpdate,
                        onSuccess = {
                            logger.log("Update sent successfully, topic: $topic")
                            onSuccess()
                        },
                        onFailure = { error ->
                            logger.error("Sending session update error: $error, topic: $topic")
                            sessionStorageRepository.deleteTempNamespacesByRequestId(sessionUpdate.id)
                            onFailure(error)
                        })
                } catch (e: Exception) {
                    logger.error("Error updating namespaces: $e")
                    onFailure(GenericException("Error updating namespaces: $e"))
                }
            },
            onFailure = {
                logger.error("Error updating namespaces: $it")
                onFailure(it)
            }
        )
    }

    private fun validate(topic: String, namespaces: Map<String, EngineDO.Namespace.Session>) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            logger.error("Sending session update error: cannot find sequence for topic: $topic")
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic))

        if (!session.isSelfController) {
            logger.error("Sending session update error: unauthorized peer")
            throw UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }

        if (!session.isAcknowledged) {
            logger.error("Sending session update error: session is not acknowledged")
            throw NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        SignValidator.validateSessionNamespace(namespaces.toMapOfNamespacesVOSession(), session.requiredNamespaces) { error ->
            logger.error("Sending session update error: invalid namespaces $error")
            throw InvalidNamespaceException(error.message)
        }
    }
}

internal interface SessionUpdateUseCaseInterface {
    suspend fun sessionUpdate(
        topic: String,
        namespaces: Map<String, EngineDO.Namespace.Session>,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}