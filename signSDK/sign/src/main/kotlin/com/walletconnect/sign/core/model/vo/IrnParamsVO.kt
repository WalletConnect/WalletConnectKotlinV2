package com.walletconnect.sign.core.model.vo

import com.walletconnect.sign.core.model.type.enums.Tags

internal data class IrnParamsVO(val tag: Tags, val ttl: TtlVO, val prompt: Boolean = false)