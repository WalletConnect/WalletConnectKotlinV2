package com.walletconnect.sign.engine.use_case.calls

import com.walletconnect.android.internal.common.exception.CannotFindSequenceForTopic
import com.walletconnect.android.internal.common.exception.Reason
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.dayInSeconds
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.NO_SEQUENCE_FOR_TOPIC_MESSAGE
import com.walletconnect.sign.common.model.vo.clientsync.session.SignRpc
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.supervisorScope

internal class DisconnectSessionUseCase(
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger,
) : DisconnectSessionUseCaseInterface {
    override suspend fun disconnect(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        if (!sessionStorageRepository.isSessionValid(Topic(topic))) {
            logger.error("Sending session disconnect error: invalid session $topic")
            return@supervisorScope onFailure(CannotFindSequenceForTopic("$NO_SEQUENCE_FOR_TOPIC_MESSAGE$topic"))
        }

        val deleteParams = SignParams.DeleteParams(Reason.UserDisconnected.code, Reason.UserDisconnected.message)
        val sessionDelete = SignRpc.SessionDelete(params = deleteParams)
        val irnParams = IrnParams(Tags.SESSION_DELETE, Ttl(dayInSeconds))

        logger.log("Sending session disconnect on topic: $topic")
        jsonRpcInteractor.publishJsonRpcRequest(Topic(topic), irnParams, sessionDelete,
            onSuccess = {
                logger.log("Disconnect sent successfully on topic: $topic")
                sessionStorageRepository.deleteSession(Topic(topic))
                jsonRpcInteractor.unsubscribe(Topic(topic))
                onSuccess()
            },
            onFailure = { error ->
                logger.error("Sending session disconnect error: $error on topic: $topic")
                onFailure(error)
            }
        )
    }
}

internal interface DisconnectSessionUseCaseInterface {
    suspend fun disconnect(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}