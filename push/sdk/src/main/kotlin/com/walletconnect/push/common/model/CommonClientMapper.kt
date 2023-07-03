package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.AppMetaData
import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.common.Push

@JvmSynthetic
internal fun Pair<EngineDO.PushSubscribe.Responded, AppMetaData?>.toCommonClient(): Push.Model.Subscription {
    val (subscription, metadata) = this

    return Push.Model.Subscription(
        subscription.requestId,
        subscription.subscribeTopic.value,
        subscription.account.value,
        RelayProtocolOptions().toClient(),
        metadata.toClient(),
        subscription.mapOfScope.toClient(),
        subscription.expiry.seconds
    )
}

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Push.Model.Subscription.Relay {
    return Push.Model.Subscription.Relay(protocol, data)
}

@JvmSynthetic
internal fun Map<String, EngineDO.PushScope.Cached>.toClient(): Map<Push.Model.Subscription.ScopeName, Push.Model.Subscription.ScopeSetting> {
    return map { (key, value) ->
        Push.Model.Subscription.ScopeName(key) to Push.Model.Subscription.ScopeSetting(value.description, value.isSelected)
    }.toMap()
}

@JvmSynthetic
internal fun SDKError.toClient(): Push.Model.Error {
    return Push.Model.Error(exception)
}