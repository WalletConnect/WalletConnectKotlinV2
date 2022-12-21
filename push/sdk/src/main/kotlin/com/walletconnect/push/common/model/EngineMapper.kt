package com.walletconnect.push.common.model

import com.walletconnect.foundation.common.model.PublicKey

@JvmSynthetic
internal fun PushParams.RequestParams.toEngineDO(id: Long): EngineDO.PushRequest =
    EngineDO.PushRequest(id, publicKey, metaData, account)

@JvmSynthetic
internal fun PushParams.MessageParams.toEngineDO(): EngineDO.PushMessage =
    EngineDO.PushMessage(title, body, icon, url)

@JvmSynthetic
internal fun PublicKey.toPushResponseParams(): PushParams.RequestResponseParams =
    PushParams.RequestResponseParams(this.keyAsHex)