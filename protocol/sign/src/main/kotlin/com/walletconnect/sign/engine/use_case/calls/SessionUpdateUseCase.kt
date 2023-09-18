package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.GenericException
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
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
        validate(topic, namespaces)

        val params = SignParams.UpdateNamespacesParams(namespaces.toMapOfNamespacesVOSession())
        val sessionUpdate = SignRpc.SessionUpdate(params = params, topic = topic)
        val irnParams = IrnParams(Tags.SESSION_UPDATE, Ttl(DAY_IN_SECONDS))

        try {
            sessionStorageRepository.insertTempNamespaces(topic, namespaces.toMapOfNamespacesVOSession(), sessionUpdate.id)
            jsonRpcInteractor.publishJsonRpcRequest(
                Topic(topic), irnParams, sessionUpdate,
                onSuccess = {
                    logger.log("Update sent successfully")
                    onSuccess()
                },
                onFailure = { error ->
                    logger.error("Sending session update error: $error")
                    sessionStorageRepository.deleteTempNamespacesByRequestId(sessionUpdate.id)
                    onFailure(error)
                })
        } catch (e: Exception) {
            onFailure(GenericException("Error updating namespaces: $e"))
        }
    }

    private fun validate(topic: String, namespaces: Map<String, EngineDO.Namespace.Session>) {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            throw CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic")
        }

        val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(Topic(topic))

        if (!session.isSelfController) {
            throw UnauthorizedPeerException(UNAUTHORIZED_UPDATE_MESSAGE)
        }

        if (!session.isAcknowledged) {
            throw NotSettledSessionException("$SESSION_IS_NOT_ACKNOWLEDGED_MESSAGE$topic")
        }

        SignValidator.validateSessionNamespace(namespaces.toMapOfNamespacesVOSession(), session.requiredNamespaces) { error ->
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