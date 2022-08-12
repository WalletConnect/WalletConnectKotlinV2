package com.walletconnect.sign.core.model.vo

import com.walletconnect.android_core.common.model.type.enums.Tags
import com.walletconnect.foundation.common.model.Ttl

internal data class IrnParamsVO(val tag: Tags, val ttl: Ttl, val prompt: Boolean = false)