package com.walletconnect.walletconnectv2.core.model.vo.jsonRpc

import com.walletconnect.walletconnectv2.core.model.type.enums.ControllerType
import com.walletconnect.walletconnectv2.storage.history.model.JsonRpcStatus

data class JsonRpcHistoryVO(
    val requestId: Long,
    val topic: String,
    val method: String?,
    val body: String?,
    val jsonRpcStatus: JsonRpcStatus,
    val controllerType: ControllerType
)