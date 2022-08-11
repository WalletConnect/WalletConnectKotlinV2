package com.walletconnect.sign.core.model.vo

import com.walletconnect.android_core.common.model.type.enums.Tags

internal data class IrnParamsVO(val tag: Tags, val ttl: TtlVO, val prompt: Boolean = false)