package com.walletconnect.walletconnectv2.relay.model.clientsync.session.common

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionState (val accounts: List<String>)