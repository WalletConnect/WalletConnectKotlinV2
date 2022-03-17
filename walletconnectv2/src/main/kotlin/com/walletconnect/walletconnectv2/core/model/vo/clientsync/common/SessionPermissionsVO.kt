package com.walletconnect.walletconnectv2.core.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.JsonRpcVO
import com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.payload.NotificationsVO

@JsonClass(generateAdapter = true)
internal data class SessionPermissionsVO(
    @Json(name = "jsonrpc")
    val jsonRpc: JsonRpcVO,
    @Json(name = "notifications")
    val notifications: NotificationsVO?,
)