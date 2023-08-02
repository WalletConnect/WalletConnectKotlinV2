@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

internal sealed class EngineDO : EngineEvent {

    sealed class Scope : EngineDO() {
        abstract val name: String
        abstract val description: String

        data class Remote(
            override val name: String,
            override val description: String,
        ) : Scope()

        data class Cached(
            override val name: String,
            override val description: String,
            val isSelected: Boolean,
        ) : Scope()
    }

    data class Record(
        val id: Long,
        val topic: String,
        val publishedAt: Long,
        val message: Message,
    ) : EngineDO()

    data class Message(
        val title: String,
        val body: String,
        val icon: String?,
        val url: String?,
        val type: String,
    ) : EngineDO()

    sealed class Subscription : EngineDO() {
        abstract val account: AccountId
        abstract val mapOfScope: Map<String, Scope.Cached>
        abstract val expiry: Expiry

        data class Requested(
            override val account: AccountId,
            override val mapOfScope: Map<String, Scope.Cached>,
            override val expiry: Expiry,
            val responseTopic: Topic,
            val subscribeTopic: Topic,
            val requestId: Long,
        ) : Subscription()

        data class Active(
            override val account: AccountId,
            override val mapOfScope: Map<String, Scope.Cached>,
            override val expiry: Expiry,
            val dappGeneratedPublicKey: PublicKey,
            val notifyTopic: Topic,
            val dappMetaData: AppMetaData? = null,
            val requestedSubscriptionId: Long? = null,
            val relay: RelayProtocolOptions = RelayProtocolOptions(),
        ) : Subscription()

        data class Error(
            val requestId: Long,
            val rejectionReason: String,
        ) : EngineDO()
    }

    sealed class Update : EngineDO() {
        data class Result(
            val account: AccountId,
            val mapOfScope: Map<String, Scope.Cached>,
            val expiry: Expiry,
            val dappGeneratedPublicKey: PublicKey,
            val notifyTopic: Topic,
            var dappMetaData: AppMetaData? = null,
            val relay: RelayProtocolOptions = RelayProtocolOptions(),
        ) : Update()

        data class Error(
            val requestId: Long,
            val rejectionReason: String,
        ) : Update()
    }


    data class Delete(val topic: String) : EngineDO()
}