package com.walletconnect.responder.ui.accounts

import androidx.annotation.DrawableRes

data class ChainAddressUI(@DrawableRes val chainIcon: Int, val chainName: String, val accountAddress: String)