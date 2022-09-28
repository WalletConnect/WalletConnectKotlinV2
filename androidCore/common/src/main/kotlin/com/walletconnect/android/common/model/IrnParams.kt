package com.walletconnect.android.common.model

import com.walletconnect.android.common.model.Tags
import com.walletconnect.foundation.common.model.Ttl

data class IrnParams(val tag: Tags, val ttl: Ttl, val prompt: Boolean = false)