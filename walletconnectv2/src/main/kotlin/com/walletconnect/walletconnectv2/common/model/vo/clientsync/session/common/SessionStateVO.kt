package com.walletconnect.walletconnectv2.common.model.vo.clientsync.session.common

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SessionStateVO (val accounts: List<String>)