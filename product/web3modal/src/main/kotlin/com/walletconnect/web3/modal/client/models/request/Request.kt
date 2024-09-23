package com.walletconnect.web3.modal.client.models.request

@Deprecated("com.walletconnect.web3.modal.client.models.request.Request has been deprecated. Please use com.reown.appkit.client.models.request.Request instead from - https://github.com/reown-com/reown-kotlin")
data class Request(
    val method: String,
    val params: String,
    val expiry: Long? = null,
)