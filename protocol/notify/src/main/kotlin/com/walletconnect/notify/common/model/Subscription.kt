@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

internal sealed class Subscription {
    abstract val account: AccountId
    abstract val mapOfScope: Map<String, Scope.Cached>
    abstract val expiry: Expiry

    data class Active(
        override val account: AccountId,
        override val mapOfScope: Map<String, Scope.Cached>,
        override val expiry: Expiry,
        val authenticationPublicKey: PublicKey,
        val topic: Topic,
        val dappMetaData: AppMetaData? = null,
        val requestedSubscriptionId: Long? = null,
        val relay: RelayProtocolOptions = RelayProtocolOptions(),
        val idOfLastNotification: String? = null,
        val reachedEndOfHistory: Boolean = false,
    ) : Subscription()
}