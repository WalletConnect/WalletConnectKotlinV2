@file:JvmSynthetic

package com.walletconnect.push.dapp.client.mapper

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.model.EngineDO

@JvmSynthetic
internal fun EngineDO.PushSubscription.toClientPushRequest(): Push.Dapp.Model.Subscription {
    return Push.Dapp.Model.Subscription(topic, relay.toClient(), metadata.toClient())
}

@JvmSynthetic
internal fun RelayProtocolOptions.toClient(): Push.Dapp.Model.Subscription.Relay {
    return Push.Dapp.Model.Subscription.Relay(protocol, data)
}

@JvmSynthetic
internal fun EngineDO.PushRequestResponse.toClientPushResponse(): Push.Dapp.Event.Response {
    // val error: String?, val subscription: Model.Subscription?
    return Push.Dapp.Event.Response(null, null)
}

@JvmSynthetic
internal fun Push.Dapp.Model.Message.toEngineDO(): EngineDO.PushMessage = EngineDO.PushMessage(title, body, icon, url)