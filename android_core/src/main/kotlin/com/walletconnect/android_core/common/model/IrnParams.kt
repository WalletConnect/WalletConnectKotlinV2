package com.walletconnect.android_core.common.model

import com.walletconnect.android_core.common.model.type.enums.Tags
import com.walletconnect.foundation.common.model.Ttl

data class IrnParams(val tag: Tags, val ttl: Ttl, val prompt: Boolean = false)