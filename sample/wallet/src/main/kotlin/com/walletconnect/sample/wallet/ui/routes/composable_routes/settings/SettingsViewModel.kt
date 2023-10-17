package com.walletconnect.sample.wallet.ui.routes.composable_routes.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.walletconnect.android.CoreClient
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.toEthAddress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    val caip10 = with(EthAccountDelegate) { account.toEthAddress() }
    val privateKey = with(EthAccountDelegate) { privateKey }
    val clientId = CoreClient.Echo.clientId

    private val _deviceToken = MutableStateFlow("")
    val deviceToken = _deviceToken.asStateFlow()

    init {
        viewModelScope.launch {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                _deviceToken.value = token
            }
        }
    }
}