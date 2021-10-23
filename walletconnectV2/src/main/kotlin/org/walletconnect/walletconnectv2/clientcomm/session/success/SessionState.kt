package org.walletconnect.walletconnectv2.clientcomm.session.success

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionState (val accounts: List<String>)