package com.walletconnect.push.wallet.engine.domain.calls

import android.util.Log
import androidx.core.net.toUri
import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.DidJwt
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.push.common.data.storage.ProposalStorageRepository
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.wallet.engine.domain.EnginePushSubscriptionNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.properties.Delegates

internal class ApproveUseCase(
    private val proposalStorageRepository: ProposalStorageRepository,
    private val subscribeToDappUseCaseInterface: SubscribeToDappUseCaseInterface,
    private val enginePushSubscriptionNotifier: EnginePushSubscriptionNotifier,
    private val crypto: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
) : ApproveUseCaseInterface {
    private var subscriptionRequestId by Delegates.notNull<Long>()
    private var didJwt by Delegates.notNull<DidJwt>()
    private var proposal by Delegates.notNull<EngineDO.PushPropose>()
    private var onSuccessFn: () -> Unit by Delegates.notNull()
    private var onFailureFn: (Throwable) -> Unit by Delegates.notNull()

    override suspend fun approve(proposalRequestId: Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val proposal = proposalStorageRepository.getProposalByRequestId(proposalRequestId) ?: return@supervisorScope onFailure(IllegalArgumentException("Invalid proposal request id"))

        this@ApproveUseCase.proposal = proposal
        this@ApproveUseCase.onSuccessFn = onSuccess
        this@ApproveUseCase.onFailureFn = onFailure

        subscribeToDappUseCaseInterface.subscribeToDapp(
            dappUri = proposal.dappMetaData.url.toUri(),
            account = proposal.accountId.value,
            onSign = onSign,
            onSuccess = { subscriptionRequestId, didJwt ->
                this@ApproveUseCase.subscriptionRequestId = subscriptionRequestId
                this@ApproveUseCase.didJwt = didJwt

                CoroutineScope(SupervisorJob() + scope.coroutineContext).launch(Dispatchers.IO) {

                    enginePushSubscriptionNotifier.newlyCreatedPushSubscription.asStateFlow()
                        .filter { subscription ->
                            subscription != null && subscription.requestId == subscriptionRequestId && subscription.subscriptionTopic != null
                        }
                        .filterNotNull()
                        .onEach { subscription ->
                            val responseTopic = Topic(sha256(proposal.dappPublicKey.keyAsBytes))
                            // Wallet generates key pair Z
                            val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
                            val symKey = crypto.getSymmetricKey(subscription.subscriptionTopic!!.value)
                            val params = PushParams.ProposeResponseParams(didJwt.value, symKey.keyAsHex)

                            jsonRpcInteractor.respondWithParams(
                                proposal.requestId,
                                responseTopic,
                                clientParams = params,
                                irnParams = IrnParams(tag = Tags.PUSH_PROPOSE_RESPONSE, ttl = Ttl(DAY_IN_SECONDS)),
                                envelopeType = EnvelopeType.ONE,
                                participants = Participants(
                                    senderPublicKey = selfPublicKey,
                                    receiverPublicKey = proposal.dappPublicKey
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

internal interface ApproveUseCaseInterface {
    suspend fun approve(proposalRequestId: kotlin.Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> kotlin.Unit, onFailure: (Throwable) -> kotlin.Unit): kotlin.Unit
}