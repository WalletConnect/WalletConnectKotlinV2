package com.walletconnect.sample.wallet.ui.routes.dialog_routes.auth_request

import com.walletconnect.web3.wallet.client.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

//todo: remove after implementing pending request
object AuthRequestStore {
    private var _activeSessions: MutableStateFlow<List<Wallet.Model.AuthRequest>> = MutableStateFlow(emptyList())
    var activeSessions: StateFlow<List<Wallet.Model.AuthRequest>> = _activeSessions.asStateFlow()

    fun addActiveSession(authRequest: Wallet.Model.AuthRequest) {
        val updatedList = _activeSessions.value.toMutableList()
        updatedList.add(authRequest)
        _activeSessions.value = updatedList
    }

    fun removeActiveSession(authRequest: Wallet.Model.AuthRequest) {
        val updatedList = _activeSessions.value.toMutableList()
        updatedList.remove(authRequest)
        _activeSessions.value = updatedList
    }


    fun removeActiveSession(id: Long) {
        val updatedList = _activeSessions.value.toMutableList()
        updatedList.removeAt(updatedList.indexOfFirst { it.id == id })
        _activeSessions.value = updatedList
    }
}