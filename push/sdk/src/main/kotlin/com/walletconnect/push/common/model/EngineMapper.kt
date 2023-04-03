package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.RelayProtocolOptions
import com.walletconnect.android.internal.common.model.params.PushParams

@JvmSynthetic
internal fun PushParams.RequestParams.toEngineDO(id: Long, topic: String, relayProtocolOptions: RelayProtocolOptions): EngineDO.PushRequest =
    EngineDO.PushRequest(id, topic, account, relayProtocolOptions, metaData)

@JvmSynthetic
internal fun PushParams.MessageParams.toEngineDO(): EngineDO.PushMessage =
    EngineDO.PushMessage(title, body, icon, url)