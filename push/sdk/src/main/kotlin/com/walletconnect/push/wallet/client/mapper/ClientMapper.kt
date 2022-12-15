package com.walletconnect.push.wallet.client.mapper

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.model.EngineDO

@JvmSynthetic
internal fun EngineDO.PushRequest.toClientPushRequest(): Push.Wallet.Model.Request {
    return Push.Wallet.Model.Request(id, metaData.toClient())
}

@JvmSynthetic
internal fun EngineDO.Message.toClientPushRequest(): Push.Wallet.Model.Message {
    return Push.Wallet.Model.Message(title, body, icon, url)
}

@JvmSynthetic
internal fun EngineDO.Subscription.toClientPushRequest(): Push.Wallet.Model.Subscription {
    return Push.Wallet.Model.Subscription(topic, relay.toClient(), metadata.toClient())
}

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Push.Wallet.Model.Subscription.Relay {
    return Push.Wallet.Model.Subscription.Relay(protocol, data)
}
