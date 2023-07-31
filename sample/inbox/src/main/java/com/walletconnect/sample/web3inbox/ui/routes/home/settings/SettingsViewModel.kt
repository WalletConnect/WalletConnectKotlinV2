package com.walletconnect.sample.web3inbox.ui.routes.home.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.walletconnect.sample.web3inbox.ui.routes.accountArg

class SettingsViewModel(
    savedStateHandle: SavedStateHandle,
    ): ViewModel() {
    val selectedAccount = checkNotNull(savedStateHandle.get<String>(accountArg))

}