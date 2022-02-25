package com.walletconnect.walletconnectv2.core.model.vo.jsonRpc

import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType

data class JsonRpcHistoryVO(
    val requestId: Long,
    val topic: String,
    val method: String,
    val body: String,
    val response: String?,
    val controllerType: ControllerType
)