package com.walletconnect.web3.modal.client.models

import com.walletconnect.web3.modal.client.Modal

data class Account(
    val address: String,
    val chain: Modal.Model.Chain
)