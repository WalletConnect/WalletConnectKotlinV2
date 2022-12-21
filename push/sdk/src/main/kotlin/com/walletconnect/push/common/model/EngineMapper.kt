package com.walletconnect.push.common.model

import com.walletconnect.android.internal.common.model.params.PushParams

@JvmSynthetic
internal fun PushParams.RequestParams.toEngineDO(id: Long): EngineDO.PushRequest =
    EngineDO.PushRequest(id, publicKey, metaData, account)

@JvmSynthetic
internal fun PushParams.MessageParams.toEngineDO(): EngineDO.PushMessage =
    EngineDO.PushMessage(title, body, icon, url)