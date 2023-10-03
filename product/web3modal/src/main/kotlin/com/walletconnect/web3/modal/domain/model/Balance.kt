package com.walletconnect.web3.modal.domain.model

import com.walletconnect.web3.modal.client.Modal
import java.math.BigDecimal

internal data class Balance(
    private val token: Modal.Model.Token,
    private val hexValue: String
) {
    private val weiFactor = BigDecimal.TEN.pow(token.decimal)

    private val value = hexValue.convertBalanceHexToBigDecimal().toWei(weiFactor)
    private val symbol = token.symbol

    val valueWithSymbol: String
        get() = "$value $symbol"
}

private fun String.convertBalanceHexToBigDecimal() = BigDecimal(this.substring(2).toLong(16).toString())

private fun BigDecimal.toWei(weiFactor: BigDecimal) = this.divide(weiFactor)
