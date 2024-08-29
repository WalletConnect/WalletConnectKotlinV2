package com.walletconnect.android.pulse.model.properties

import com.squareup.moshi.Json
import com.walletconnect.android.pulse.model.EventType
import com.walletconnect.utils.Empty

data class Props(
    @Json(name = "event")
    val event: String = EventType.ERROR,
    @Json(name = "type")
    val type: String = String.Empty,
    @Json(name = "properties")
    val properties: Properties? = null
)