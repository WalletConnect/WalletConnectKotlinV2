package com.walletconnect.push.engine.calls

import androidx.core.net.toUri
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.AppMetaDataType
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.storage.MetadataStorageRepositoryInterface
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.push.common.data.storage.ProposalStorageRepository
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.engine.domain.EnginePushSubscriptionNotifier
import com.walletconnect.utils.Empty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class ApproveSubscriptionRequestUseCase(
    private val subscribeUseCase: SubscribeToDappUseCaseInterface,
    private val proposalStorageRepository: ProposalStorageRepository,
    private val metadataStorageRepository: MetadataStorageRepositoryInterface,
    private val crypto: KeyManagementRepository,
    private val enginePushSubscriptionNotifier: EnginePushSubscriptionNotifier,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
): ApproveSubscriptionRequestUseCaseInterface {

    override suspend fun approve(proposalRequestId: Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val proposalWithoutMetadata = proposalStorageRepository.getProposalByRequestId(proposalRequestId) ?: return@supervisorScope onFailure(IllegalArgumentException("Invalid proposal request id $proposalRequestId"))
        val dappMetadata: AppMetaData? = metadataStorageRepository.getByTopicAndType(proposalWithoutMetadata.proposalTopic, AppMetaDataType.PEER)
        val proposalWithMetadata = with(proposalWithoutMetadata) {
            EngineDO.PushProposal(requestId, proposalTopic, dappPublicKey, accountId, relayProtocolOptions, dappMetadata)
        }

        // Wallet sends push subscribe request to Push Server with subscriptionAuth
        subscribeUseCase.subscribeToDapp(
            dappUri = proposalWithMetadata.dappMetadata?.url?.toUri() ?: String.Empty.toUri(),
            account = proposalWithMetadata.accountId.value,
            onSign = onSign,
            onSuccess = { requestId, didJwt ->
                CoroutineScope(SupervisorJob() + scope.coroutineContext).launch(Dispatchers.IO) {
                    enginePushSubscriptionNotifier.newlyRespondedRequestedSubscriptionId.asStateFlow()
                        .filterNotNull()
                        .filter { (id, _) -> id == requestId }
                        .map { (_, activeSubscription) -> activeSubscription }
                        .onEach { activeSubscription ->
                            // Wallet generates key pair Z
                            val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
                            val symKey = crypto.getSymmetricKey(activeSubscription.pushTopic.value.lowercase())
                            val params = PushParams.ProposeResponseParams(didJwt.value, symKey.keyAsHex)

                            // Wallet responds with type 1 envelope on response topic to Dapp with subscriptionAuth and subscription symmetric key
                            jsonRpcInteractor.respondWithParams(
                                proposalWithMetadata.requestId,
                                Topic(sha256(proposalWithMetadata.dappPublicKey.keyAsBytes)),
                                clientParams = params,
                                irnParams = IrnParams(tag = Tags.PUSH_PROPOSE_RESPONSE, ttl = Ttl(DAY_IN_SECONDS)),
                                envelopeType = EnvelopeType.ONE,
                                participants = Participants(
                                    senderPublicKey = selfPublicKey,
                                    receiverPublicKey = proposalWithMetadata.dappPublicKey
                                )
                            ) { error ->
                                return@respondWithParams onFailure(error)
                            }

                            onSuccess()
                        }.launchIn(this)
                }
            },
            onFailure = {
                onFailure(it)
            }
        )
    }

}

internal interface ApproveSubscriptionRequestUseCaseInterface {
    suspend fun approve(proposalRequestId: Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}