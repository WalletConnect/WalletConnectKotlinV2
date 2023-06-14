package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

sealed class EngineDO : EngineEvent {

    data class PushRequest(
        val id: Long,
        val topic: String,
        val account: String,
        val relay: RelayProtocolOptions,
        val metaData: AppMetaData,
    ) : EngineDO()

    sealed class PushScope : EngineDO() {
        abstract val name: String
        abstract val description: String

        data class Remote(
            override val name: String,
            override val description: String,
        ) : PushScope()

        data class Cached(
            override val name: String,
            override val description: String,
            val isSelected: Boolean,
        ) : PushScope()
    }

    data class PushPropose(
        val requestId: Long,
        val proposalTopic: Topic,
        val dappPublicKey: PublicKey,
        val accountId: AccountId,
        val relayProtocolOptions: RelayProtocolOptions,
        val dappMetaData: AppMetaData,
    ) : EngineDO()

    data class PushRecord(
        val id: Long,
        val topic: String,
        val publishedAt: Long,
        val message: PushMessage,
    ) : EngineDO()

    data class PushMessage(
        val title: String,
        val body: String,
        val icon: String?,
        val url: String?,
        val type: String,
    ) : EngineDO()

    data class PushSubscription(
        val requestId: Long,
        val keyAgreementTopic: Topic,
        val responseTopic: Topic,
        val peerPublicKey: PublicKey?,
        val subscriptionTopic: Topic?,
        val account: AccountId,
        val relay: RelayProtocolOptions,
        val metadata: AppMetaData,
        val didJwt: String,
        val scope: Map<String, PushScope.Cached>,
        val expiry: Expiry,
    ) : EngineDO()

    data class PushUpdate(
        val requestId: Long,
        val responseTopic: String,
        val peerPublicKeyAsHex: String?,
        val subscriptionTopic: String?,
        val account: AccountId,
        val relay: RelayProtocolOptions,
        val metadata: AppMetaData,
        val didJwt: String,
        val scope: Map<String, PushScope.Cached>,
        val expiry: Expiry,
    ) : EngineDO()

    data class PushRequestResponse(
        val subscription: PushSubscription,
    ) : EngineDO()

    data class PushRequestRejected(
        val requestId: Long,
        val rejectionReason: String,
    ) : EngineDO()

    data class PushSubscribeError(
        val requestId: Long,
        val rejectionReason: String,
    ) : EngineDO()

    data class PushUpdateError(
        val requestId: Long,
        val rejectionReason: String,
    ) : EngineDO()

    data class PushDelete(val topic: String) : EngineDO()
}
