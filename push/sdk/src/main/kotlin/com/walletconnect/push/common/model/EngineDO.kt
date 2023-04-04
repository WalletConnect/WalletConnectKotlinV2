package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.type.EngineEvent

sealed class EngineDO : EngineEvent {

    data class PushRequest(
        val id: Long,
        val topic: String,
        val account: String,
        val relay: RelayProtocolOptions,
        val metaData: AppMetaData,
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
    ) : EngineDO()

    data class PushSubscription(
        val requestId: Long,
        val pairingTopic: String,
        val peerPublicKeyAsHex: String,
        val topic: String?,
        val account: AccountId,
        val relay: RelayProtocolOptions,
        val metadata: AppMetaData,
    ) : EngineDO()

    data class PushRequestResponse(
        val subscription: PushSubscription,
    ) : EngineDO()

    data class PushRequestRejected(
        val requestId: Long,
        val rejectionReason: String,
    ) : EngineDO()

    data class PushDelete(val topic: String) : EngineDO()
}
