@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

internal sealed class Subscription {
    abstract val account: AccountId
    abstract val mapOfNotificationScope: Map<String, NotificationScope.Cached>
    abstract val expiry: Expiry

    data class Requested(
        override val account: AccountId,
        override val mapOfNotificationScope: Map<String, NotificationScope.Cached>,
        override val expiry: Expiry,
        val responseTopic: Topic,
        val subscribeTopic: Topic,
        val requestId: Long,
        val authenticationPublicKey: PublicKey,
    ) : Subscription()

    data class Active(
        override val account: AccountId,
        override val mapOfNotificationScope: Map<String, NotificationScope.Cached>,
        override val expiry: Expiry,
        val authenticationPublicKey: PublicKey,
        val dappGeneratedPublicKey: PublicKey,
        val notifyTopic: Topic,
        val dappMetaData: AppMetaData? = null,
        val requestedSubscriptionId: Long? = null,
        val relay: RelayProtocolOptions = RelayProtocolOptions(),
    ) : Subscription(), EngineEvent
}