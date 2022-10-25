package com.walletconnect.android.internal.common.model

import com.walletconnect.foundation.common.model.Ttl

data class IrnParams(val tag: Tags, val ttl: Ttl, val prompt: Boolean = false)