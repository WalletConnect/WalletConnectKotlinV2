@file:JvmSynthetic

package com.walletconnect.chat.engine.model

import com.walletconnect.chat.core.model.vo.MediaVO

internal fun EngineDO.Media.toMediaVO(): MediaVO {
    return MediaVO(type, data)
}