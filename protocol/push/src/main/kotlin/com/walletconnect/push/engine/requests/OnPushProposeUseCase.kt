package com.walletconnect.push.engine.requests

import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.push.common.data.storage.ProposalStorageRepository
import com.walletconnect.push.common.model.EngineDO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

// Spec: https://docs.walletconnect.com/2.0/specs/clients/push/push-proposal#protocol
internal class OnPushProposeUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: PushParams.ProposeParams) = supervisorScope {
        try {
            metadataStorageRepository.insertOrAbortMetadata(
                request.topic,
                params.metaData,
                AppMetaDataType.PEER
            )
        } catch (e: Exception) {
            logger.error("Cannot insert metadata: ${e.message}")
            return@supervisorScope _events.emit(SDKError(e))
        }

        try {
            proposalStorageRepository.insertProposal(
                requestId = request.id,
                proposalTopic = request.topic.value,
                dappPublicKeyAsHex = params.publicKey,
                accountId = params.account,
            )

            _events.emit(
                EngineDO.PushProposal(
                    request.id,
                    Topic(request.topic.value),
                    PublicKey(params.publicKey),
                    AccountId(params.account),
                    RelayProtocolOptions(),
                    params.metaData
                )
            )
        } catch (e: Exception) {
            jsonRpcInteractor.respondWithError(
                request,
                Uncategorized.GenericError("Cannot handle the push request: ${e.message}, topic: ${request.topic}"),
                IrnParams(Tags.PUSH_PROPOSE_RESPONSE, Ttl(DAY_IN_SECONDS))
            )

            _events.emit(SDKError(e))
        }
    }
}

