package com.walletconnect.web3.modal.domain.model

import com.walletconnect.web3.modal.client.Modal

internal data class AccountData(
    val address: String,
    val topic: String,
    val balance: String?,
    val chains: List<Modal.Model.Chain>,
    val identity: Identity? = null
)
