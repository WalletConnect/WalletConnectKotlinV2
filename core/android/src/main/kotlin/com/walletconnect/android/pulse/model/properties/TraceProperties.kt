package com.walletconnect.android.pulse.model.properties

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.android.pulse.model.Trace

@JsonClass(generateAdapter = true)
data class TraceProperties(
	@Json(name = "trace")
	val trace: List<String>? = null,
	@Json(name = "topic")
	val topic: String? = null,
)