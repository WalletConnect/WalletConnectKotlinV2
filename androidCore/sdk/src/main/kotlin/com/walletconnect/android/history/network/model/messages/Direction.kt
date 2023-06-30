package com.walletconnect.android.history.network.model.messages

import com.squareup.moshi.Json

enum class Direction {
    @Json(name = "forward")
    FORWARD,
    @Json(name = "backward")
    BACKWARD;

    override fun toString(): String {
        return name.lowercase()
    }
}