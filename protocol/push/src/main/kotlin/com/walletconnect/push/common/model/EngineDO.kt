package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

sealed class EngineDO : EngineEvent {

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

    data class PushProposal(
        val requestId: Long,
        val proposalTopic: Topic,
        val dappPublicKey: PublicKey,
        val accountId: AccountId,
        val relayProtocolOptions: RelayProtocolOptions,
        val dappMetadata: AppMetaData? = null,
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

    sealed class Subscription : EngineDO() {
        abstract val account: AccountId
        abstract val mapOfScope: Map<String, PushScope.Cached>
        abstract val expiry: Expiry

        data class Requested(
            override val account: AccountId,
            override val mapOfScope: Map<String, PushScope.Cached>,
            override val expiry: Expiry,
            val responseTopic: Topic,
            val subscribeTopic: Topic,
            val requestId: Long,
        ) : Subscription()

        data class Active(
            override val account: AccountId,
            override val mapOfScope: Map<String, PushScope.Cached>,
            override val expiry: Expiry,
            val dappGeneratedPublicKey: PublicKey,
            val pushTopic: Topic,
            val dappMetaData: AppMetaData? = null,
            val requestedSubscriptionId: Long? = null,
            val relay: RelayProtocolOptions = RelayProtocolOptions(),
        ) : Subscription()

        data class Error(
            val requestId: Long,
            val rejectionReason: String,
        ) : EngineDO()
    }

    sealed class PushUpdate : EngineDO() {
        data class Result(
            val account: AccountId,
            val mapOfScope: Map<String, PushScope.Cached>,
            val expiry: Expiry,
            val dappGeneratedPublicKey: PublicKey,
            val pushTopic: Topic,
            var dappMetaData: AppMetaData? = null,
            val relay: RelayProtocolOptions = RelayProtocolOptions(),
        ) : PushUpdate()

        data class Error(
            val requestId: Long,
            val rejectionReason: String,
        ) : PushUpdate()
    }


    data class PushLegacySubscription(
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

    data class PushDelete(val topic: String) : EngineDO()
}