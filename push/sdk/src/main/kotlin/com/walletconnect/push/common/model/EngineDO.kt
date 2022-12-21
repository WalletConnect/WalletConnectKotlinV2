package com.walletconnect.push.common.model

import com.walletconnect.android.impl.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.RelayProtocolOptions

sealed class EngineDO {

    data class PushRequest(
        val id: Long,
        val publicKey: String,
        val metaData: AppMetaData,
        val account: String,
    ) : EngineDO(), EngineEvent

    data class PushMessage(
        val title: String,
        val body: String,
        val icon: String,
        val url: String,
    ): EngineDO(), EngineEvent

    data class PushSubscription(
        val topic: String,
        val relay: RelayProtocolOptions,
        val metadata: AppMetaData,
    ): EngineDO() {

    }
}
