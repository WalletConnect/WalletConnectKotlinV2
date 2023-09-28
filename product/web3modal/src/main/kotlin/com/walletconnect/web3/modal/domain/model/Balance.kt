package com.walletconnect.web3.modal.domain.model

import com.walletconnect.web3.modal.client.Modal
import java.math.BigDecimal

internal data class Balance(
    private val token: Modal.Model.Token,
    private val hexValue: String
) {
    private val factor = BigDecimal.TEN.pow(token.decimal)

    private val value = BigDecimal(hexValue.substring(2).toLong(16).toString()).divide(factor)
    private val symbol = token.symbol

    val valueWithSymbol: String
        get() = "$value $symbol"
}
