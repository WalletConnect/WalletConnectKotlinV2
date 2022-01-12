package com.walletconnect.walletconnectv2.common.model.vo.sequence.lifecycle

import com.walletconnect.walletconnectv2.common.model.type.SequenceLifecycle

data class SessionRequestVO(
    val topic: String,
    val chainId: String?,
    val request: JSONRPCRequestVO
) : SequenceLifecycle {

    data class JSONRPCRequestVO(
        val id: Long,
        val method: String,
        val params: String
    )
}