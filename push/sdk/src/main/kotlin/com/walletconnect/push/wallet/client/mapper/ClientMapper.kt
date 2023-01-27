package com.walletconnect.push.wallet.client.mapper

import com.walletconnect.android.pairing.model.mapper.toClient
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.model.EngineDO

@JvmSynthetic
internal fun EngineDO.PushRequest.toClient(): Push.Wallet.Event.Request {
    return Push.Wallet.Event.Request(id, metaData.toClient())
}

@JvmSynthetic
internal fun EngineDO.PushMessage.toClientEvent(): Push.Wallet.Event.Message {
    return Push.Wallet.Event.Message(title, body, icon, url)
}

@JvmSynthetic
internal fun EngineDO.PushMessage.toClientModel(): Push.Model.Message {
    return Push.Model.Message(title, body, icon, url)
}