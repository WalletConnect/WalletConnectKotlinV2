package com.walletconnect.foundation.common.model

import com.squareup.moshi.JsonClass
import com.walletconnect.util.Empty

@JsonClass(generateAdapter = false)
data class Topic(val value: String = String.Empty)