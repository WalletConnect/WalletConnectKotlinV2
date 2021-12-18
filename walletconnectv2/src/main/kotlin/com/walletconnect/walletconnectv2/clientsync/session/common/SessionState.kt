package com.walletconnect.walletconnectv2.clientsync.session.common

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionState (val accounts: List<String>)