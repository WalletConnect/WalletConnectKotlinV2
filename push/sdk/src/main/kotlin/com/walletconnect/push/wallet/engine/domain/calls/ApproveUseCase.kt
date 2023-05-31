package com.walletconnect.push.wallet.engine.domain.calls

import com.walletconnect.android.internal.common.crypto.kmr.KeyManagementRepository
import com.walletconnect.android.internal.common.crypto.sha256
import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.Participants
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.params.PushParams
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.utils.DAY_IN_SECONDS
import com.walletconnect.foundation.common.model.Topic
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.push.common.calcExpiry
import com.walletconnect.push.common.data.storage.SubscriptionStorageRepository
import kotlinx.coroutines.supervisorScope

internal class ApproveUseCase(
    private val subscriptionStorageRepository: SubscriptionStorageRepository,
    private val registerIdentityAndReturnDidJwtUseCase: RegisterIdentityAndReturnDidJwtUseCase,
    private val crypto: KeyManagementRepository,
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
): ApproveUseCaseInterface {

    override suspend fun approve(requestId: Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) = supervisorScope {
        val respondedSubscription = subscriptionStorageRepository.getSubscriptionsByRequestId(requestId)
        val dappPublicKey = respondedSubscription.peerPublicKey ?: return@supervisorScope onFailure(IllegalArgumentException("Invalid dapp public key"))
        val responseTopic = sha256(dappPublicKey.keyAsBytes)

        val didJwt = registerIdentityAndReturnDidJwtUseCase(respondedSubscription.account, respondedSubscription.metadata.url, emptyList(), onSign, onFailure).getOrElse { error ->
            return@supervisorScope onFailure(error)
        }
        val selfPublicKey = crypto.generateAndStoreX25519KeyPair()
        val pushTopic = crypto.generateTopicFromKeyAgreement(selfPublicKey, dappPublicKey)
        val approvalParams = PushParams.RequestResponseParams(didJwt.value)
        val irnParams = IrnParams(Tags.PUSH_REQUEST_RESPONSE, Ttl(DAY_IN_SECONDS))

        subscriptionStorageRepository.updateSubscriptionToRespondedByApproval(responseTopic, pushTopic.value, calcExpiry())

        jsonRpcInteractor.subscribe(pushTopic) jsonSubscribe@{ error ->
            return@jsonSubscribe onFailure(error)
        }

        jsonRpcInteractor.respondWithParams(
            respondedSubscription.requestId,
            Topic(responseTopic),
            approvalParams,
            irnParams,
            envelopeType = EnvelopeType.ONE,
            participants = Participants(selfPublicKey, dappPublicKey)
        ) { error ->
            return@respondWithParams onFailure(error)
        }

        onSuccess()
    }
}

internal interface ApproveUseCaseInterface {
    suspend fun approve(requestId: Long, onSign: (String) -> Cacao.Signature?, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}