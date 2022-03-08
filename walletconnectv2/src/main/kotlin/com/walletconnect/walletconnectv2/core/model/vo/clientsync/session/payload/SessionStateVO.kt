package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SessionStateVO(val accounts: List<String>)