package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.common.Push

@JvmSynthetic
internal fun EngineDO.PushSubscription.Responded.toClient(): Push.Model.Subscription {
    return Push.Model.Subscription(requestId, topic, relay.toClient(), metadata?.toClient())
}

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Push.Model.Subscription.Relay {
    return Push.Model.Subscription.Relay(protocol, data)
}

@JvmSynthetic
internal fun SDKError.toClient(): Push.Model.Error {
    return Push.Model.Error(exception)
}