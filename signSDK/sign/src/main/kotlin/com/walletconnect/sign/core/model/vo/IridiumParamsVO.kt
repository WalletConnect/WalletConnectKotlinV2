package com.walletconnect.sign.core.model.vo

import com.walletconnect.sign.core.model.type.enums.Tags

internal data class IridiumParamsVO(val tag: Tags, val ttl: TtlVO, val prompt: Boolean = false)