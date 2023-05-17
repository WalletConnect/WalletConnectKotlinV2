package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.common.Push

@JvmSynthetic
internal fun EngineDO.PushSubscription.toCommonClient(): Push.Model.Subscription {
    return Push.Model.Subscription(requestId, subscriptionTopic!!.value, account.value, relay.toClient(), metadata.toClient(), scope.toClient(), expiry.seconds)
}

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Push.Model.Subscription.Relay {
    return Push.Model.Subscription.Relay(protocol, data)
}

@JvmSynthetic
internal fun Map<String, Pair<String, Boolean>>.toClient(): Map<Push.Model.Subscription.ScopeName, Push.Model.Subscription.ScopeSetting> {
    return map { (key, value) ->
        Push.Model.Subscription.ScopeName(key) to Push.Model.Subscription.ScopeSetting(value.first, value.second)
    }.toMap()
}

@JvmSynthetic
internal fun SDKError.toClient(): Push.Model.Error {
    return Push.Model.Error(exception)
}