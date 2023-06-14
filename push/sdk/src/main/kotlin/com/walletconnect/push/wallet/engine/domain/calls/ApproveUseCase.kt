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
    private val subscribeJob: Job? = null
    // <editor-fold desc="old">
    private var subscriptionRequestId by Delegates.notNull<Long>()
    private var didJwt by Delegates.notNull<DidJwt>()
    private var proposal by Delegates.notNull<EngineDO.PushPropose>()
    private var onSuccessFn: () -> Unit by Delegates.notNull()
    private var onFailureFn: (Throwable) -> Unit by Delegates.notNull()
//    private val collectPushSubscriptionJob: Job by lazy {
//        enginePushSubscriptionNotifier.newlyCreatedPushSubscription
////                                    .filterNotNull()
//            .filter { subscription ->
//                Log.e("Talha18", "filtering: value: $subscription and requestId: $subscriptionRequestId")
//                subscription != null && subscription.requestId == subscriptionRequestId && subscription.subscriptionTopic != null
//            }
//            .filterNotNull()
//            .onEach { subscription ->
//                Log.e("Talha19", "onEach: value: $subscription")
//                val responseTopic = Topic(sha256(proposal.dappPublicKey.keyAsBytes))
//                // Wallet generates key pair Z
//                val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
//                val symKey = crypto.getSymmetricKey(subscription.subscriptionTopic!!.value)
//                val params = PushParams.ProposeResponseParams(didJwt.value, symKey.keyAsHex)
//
//                Log.e("Talha20", "sending response: $params on topic: $responseTopic with requestId: ${proposal.requestId}")
//                jsonRpcInteractor.respondWithParams(
//                    proposal.requestId,
//                    responseTopic,
//                    clientParams = params,
//                    irnParams = IrnParams(tag = Tags.PUSH_PROPOSE_RESPONSE, ttl = Ttl(DAY_IN_SECONDS)),
//                    envelopeType = EnvelopeType.ONE,
//                    participants = Participants(
//                        senderPublicKey = selfPublicKey,
//                        receiverPublicKey = proposal.dappPublicKey
//                    )
//                ) { error ->
//                    Log.e("Talha21", "error: $error")
//                    return@respondWithParams this.onFailureFn(error)
//                }
//
//                Log.e("Talha22", "response sent")
//                onSuccessFn()
//            }.launchIn(scope)
//    }
    // </editor-fold>

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
                Log.e("TalhaRequestId3", subscriptionRequestId.toString())
                this@ApproveUseCase.didJwt = didJwt

                CoroutineScope(SupervisorJob() + scope.coroutineContext).launch(Dispatchers.IO) {
                    // <editor-fold desc="logging start of coroutine">
                    Log.e("Talha32", "Inside Launch/scope: ${this.coroutineContext.job.isActive}")
                    Log.e("Talha45", enginePushSubscriptionNotifier.newlyCreatedPushSubscription.value.toString())
                    // </editor-fold>

                    enginePushSubscriptionNotifier.newlyCreatedPushSubscription.asStateFlow()
                            // <editor-fold desc="filtering">
                        .filter { subscription ->
                            Log.e("Talha18", "filtering: value: $subscription and requestId: $subscriptionRequestId")
                            subscription != null && subscription.requestId == subscriptionRequestId && subscription.subscriptionTopic != null
                        }
                        .filterNotNull()
                            // </editor-fold>
                        .onEach { subscription ->
                            Log.e("Talha19", "onEach: value: $subscription")
                            val responseTopic = Topic(sha256(proposal.dappPublicKey.keyAsBytes))
                            // Wallet generates key pair Z
                            val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
                            val symKey = crypto.getSymmetricKey(subscription.subscriptionTopic!!.value)
                            val params = PushParams.ProposeResponseParams(didJwt.value, symKey.keyAsHex)

                            Log.e("Talha20", "sending response: $params on topic: $responseTopic with requestId: ${proposal.requestId}")
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
                                Log.e("Talha21", "error: $error")
                                return@respondWithParams onFailure(error)
                            }

                            Log.e("Talha22", "response sent")
                            onSuccess()
                        }.launchIn(this)
                    Log.e("Talha46", "completing the job")
                }
// <editor-fold desc="Old Code">
//                Log.e("Talha27", "engineSubscriptionNotifier flow count: ${enginePushSubscriptionNotifier.newlyCreatedPushSubscription.subscriptionCount.value}")
//                if (collectPushSubscriptionJob == null) {
//                    Log.e("Talha15", "creating job for first time")
////                    collectPushSubscriptionJob = launch {
//                    try {
////                            withTimeout(TimeUnit.SECONDS.toMillis(20)) {
////                                Log.e("Talha17", "Starting timeout")
//                        Log.e("Talha16", "engineSubscriptionNotifier: $enginePushSubscriptionNotifier")
//                        Log.e("Talha17", "engineSubscriptionNotifier flow: ${enginePushSubscriptionNotifier.newlyCreatedPushSubscription}")
//                        Log.e(
//                            "Talha30",
//                            "localSubscriptionRequestId: ${this@ApproveUseCase.subscriptionRequestId}\ndidJwt: ${this@ApproveUseCase.didJwt}\nonSuccessFn: ${this@ApproveUseCase.onSuccessFn}"
//                        )
//                        /*collectPushSubscriptionJob = */enginePushSubscriptionNotifier.newlyCreatedPushSubscription
////                                    .filterNotNull()
//                            .filter { subscription ->
//                                Log.e("Talha18", "filtering: value: $subscription and requestId: $subscriptionRequestId")
//                                subscription != null && subscription.requestId == subscriptionRequestId && subscription.subscriptionTopic != null
//                            }
//                            .filterNotNull()
//                            .onEach { subscription ->
//                                Log.e("Talha19", "onEach: value: $subscription")
//                                val responseTopic = Topic(sha256(proposal.dappPublicKey.keyAsBytes))
//                                // Wallet generates key pair Z
//                                val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
//                                val symKey = crypto.getSymmetricKey(subscription.subscriptionTopic!!.value)
//                                val params = PushParams.ProposeResponseParams(didJwt.value, symKey.keyAsHex)
//
//                                Log.e("Talha20", "sending response: $params on topic: $responseTopic with requestId: $proposalRequestId")
//                                jsonRpcInteractor.respondWithParams(
//                                    proposalRequestId,
//                                    responseTopic,
//                                    clientParams = params,
//                                    irnParams = IrnParams(tag = Tags.PUSH_PROPOSE_RESPONSE, ttl = Ttl(DAY_IN_SECONDS)),
//                                    envelopeType = EnvelopeType.ONE,
//                                    participants = Participants(
//                                        senderPublicKey = selfPublicKey,
//                                        receiverPublicKey = proposal.dappPublicKey
//                                    )
//                                ) { error ->
//                                    Log.e("Talha21", "error: $error")
//                                    return@respondWithParams onFailure(error)
//                                }
//
//                                Log.e("Talha22", "response sent")
//                                onSuccess()
//                            }.launchIn(this)
////                            }
//                    } catch (e: TimeoutCancellationException) {
//                        Log.e("Talha23", "timeout")
//                        return@subscribeToDapp onFailure(e)
//                    }
////                    }
//                } else {
//                    Log.e("Talha16", "job already exists")
//                }

//                Log.e("Talha29", "engineSubscriptionNotifier flow count: ${enginePushSubscriptionNotifier.newlyCreatedPushSubscription.subscriptionCount.value}")
// </editor-fold>
            },
            onFailure = {
                Log.e("Talha24", "onFailure: $it")
                onFailure(it)
            }
        )

        Log.e("Talha35", this.coroutineContext.job.isCompleted.toString())
        kotlin.Unit
    }
}

internal interface ApproveUseCaseInterface {
    suspend fun approve(proposalRequestId: kotlin.Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> kotlin.Unit, onFailure: (Throwable) -> kotlin.Unit): kotlin.Unit
}