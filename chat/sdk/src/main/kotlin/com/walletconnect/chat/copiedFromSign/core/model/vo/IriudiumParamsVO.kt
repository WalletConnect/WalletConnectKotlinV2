package com.walletconnect.chat.copiedFromSign.core.model.vo

import com.walletconnect.chat.core.model.vo.Tags

internal data class IrnParamsVO(val tag: Tags, val ttl: TtlVO, val prompt: Boolean = false)