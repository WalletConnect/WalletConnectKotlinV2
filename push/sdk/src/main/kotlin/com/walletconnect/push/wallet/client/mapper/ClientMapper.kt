package com.walletconnect.push.wallet.client.mapper

import com.walletconnect.push.common.Push
import com.walletconnect.push.common.model.EngineDO

@JvmSynthetic
internal fun EngineDO.PushRequest.toClientPushRequest(): Push.Wallet.Model.Request {
    return Push.Wallet.Model.Request()
}

@JvmSynthetic
internal fun EngineDO.Message.toClientPushRequest(): Push.Wallet.Model.Message {
    return Push.Wallet.Model.Message()
}

@JvmSynthetic
internal fun EngineDO.Subscription.toClientPushRequest(): Push.Wallet.Model.Subscription {
    return Push.Wallet.Model.Subscription()
}