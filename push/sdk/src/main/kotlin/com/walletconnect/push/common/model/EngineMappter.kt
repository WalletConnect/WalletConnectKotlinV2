package com.walletconnect.push.common.model

import com.walletconnect.foundation.common.model.PublicKey

@JvmSynthetic
internal fun PushParams.RequestParams.toEngineDO(): EngineDO.PushRequest =
    EngineDO.PushRequest(publicKey, metaData, account)

@JvmSynthetic
internal fun PushParams.MessageParams.toEngineDO(): EngineDO.Message =
    EngineDO.Message(title, body, icon, url)

@JvmSynthetic
internal fun PublicKey.toPushResponseParams(): PushParams.RequestResponseParams =
    PushParams.RequestResponseParams(this.keyAsHex)