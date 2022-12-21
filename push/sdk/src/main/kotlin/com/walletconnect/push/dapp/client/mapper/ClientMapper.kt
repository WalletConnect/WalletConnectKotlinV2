@file:JvmSynthetic

package com.walletconnect.push.dapp.client.mapper

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.model.EngineDO

@JvmSynthetic
internal fun EngineDO.PushSubscription.Responded.toClient(): Push.Model.Subscription {
    return Push.Model.Subscription(requestId, topic, relay.toClient(), metadata?.toClient())
}

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Push.Model.Subscription.Relay {
    return Push.Model.Subscription.Relay(protocol, data)
}

@JvmSynthetic
internal fun EngineDO.PushRequestResponse.toClient(): Push.Dapp.Event.Response {
    return Push.Dapp.Event.Response(Push.Model.Subscription(subscription.requestId, subscription.topic, subscription.relay.toClient(), subscription.metadata?.toClient()))
}

@JvmSynthetic
internal fun EngineDO.PushDelete.toClient(): Push.Dapp.Event.Delete {
    return Push.Dapp.Event.Delete(topic)
}

@JvmSynthetic
internal fun EngineDO.PushRequestRejected.toClient(): Push.Dapp.Event.Rejected {
    return Push.Dapp.Event.Rejected(rejectionReason)
}

@JvmSynthetic
internal fun Push.Model.Message.toEngineDO(): EngineDO.PushMessage = EngineDO.PushMessage(title, body, icon, url)