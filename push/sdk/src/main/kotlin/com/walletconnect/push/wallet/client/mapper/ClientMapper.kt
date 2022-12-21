package com.walletconnect.push.wallet.client.mapper

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.model.EngineDO

@JvmSynthetic
internal fun EngineDO.PushRequest.toClient(): Push.Wallet.Event.Request {
    return Push.Wallet.Event.Request(id, metaData.toClient())
}

@JvmSynthetic
internal fun EngineDO.PushMessage.toClient(): Push.Wallet.Event.Message {
    return Push.Wallet.Event.Message(title, body, icon, url)
}

@JvmSynthetic
internal fun EngineDO.PushSubscription.Responded.toClient(): Push.Model.Subscription {
    return Push.Model.Subscription(requestId, topic, relay.toClient(), metadata?.toClient())
}

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Push.Model.Subscription.Relay {
    return Push.Model.Subscription.Relay(protocol, data)
}
