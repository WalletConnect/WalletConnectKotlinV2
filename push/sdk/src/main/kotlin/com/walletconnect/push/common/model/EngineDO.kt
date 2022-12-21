package com.walletconnect.push.common.model

import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.RelayProtocolOptions

sealed class EngineDO: EngineEvent {

    data class PushRequest(
        val id: Long,
        val publicKey: String,
        val metaData: AppMetaData,
        val account: String,
    ) : EngineDO()

    data class PushMessage(
        val title: String,
        val body: String,
        val icon: String,
        val url: String,
    ) : EngineDO()

    sealed class PushSubscription : EngineDO() {
        abstract val requestId: Long
        abstract val peerPublicKey: String

        data class Requested(override val requestId: Long, override val peerPublicKey: String) : PushSubscription()

        data class Responded(override val requestId: Long, override val peerPublicKey: String, val topic: String, val relay: RelayProtocolOptions, val metadata: AppMetaData?) : PushSubscription()
    }

    data class PushRequestResponse(
        val subscription: PushSubscription.Responded
    ) : EngineDO()

    data class PushRequestRejected(
        val requestId: Long,
        val rejectionReason: String
    ) : EngineDO()

    data class PushDelete(val topic: String): EngineDO()
}
