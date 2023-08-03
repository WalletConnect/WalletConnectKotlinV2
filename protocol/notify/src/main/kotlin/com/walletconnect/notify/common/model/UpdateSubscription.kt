@file:JvmSynthetic

package com.walletconnect.notify.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.common.model.Topic

internal sealed class UpdateSubscription : EngineEvent {

    data class Result(
        val account: AccountId,
        val mapOfNotificationScope: Map<String, NotificationScope.Cached>,
        val expiry: Expiry,
        val dappGeneratedPublicKey: PublicKey,
        val notifyTopic: Topic,
        var dappMetaData: AppMetaData? = null,
        val relay: RelayProtocolOptions = RelayProtocolOptions(),
    ) : UpdateSubscription()

    data class Error(
        val requestId: Long,
        val rejectionReason: String,
    ) : UpdateSubscription()
}