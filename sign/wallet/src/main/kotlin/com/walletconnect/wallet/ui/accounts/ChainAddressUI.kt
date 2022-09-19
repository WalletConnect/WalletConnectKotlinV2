package com.walletconnect.wallet.ui.accounts

import androidx.annotation.DrawableRes

data class AccountsUI(val isSelected: Boolean, val title: String, val chainAddressList: List<ChainAddressUI>)
data class ChainAddressUI(@DrawableRes val chainIcon: Int, val chainName: String, val accountAddress: String)