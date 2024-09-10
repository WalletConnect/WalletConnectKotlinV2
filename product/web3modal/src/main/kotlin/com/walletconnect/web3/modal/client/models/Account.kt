package com.walletconnect.web3.modal.client.models

import com.walletconnect.web3.modal.client.Modal

@Deprecated("com.walletconnect.web3.modal.client.models.Account has been deprecated. Please use com.reown.appkit.client.models.Account instead from - https://github.com/reown-com/reown-kotlin")
data class Account(
    val address: String,
    val chain: Modal.Model.Chain
)