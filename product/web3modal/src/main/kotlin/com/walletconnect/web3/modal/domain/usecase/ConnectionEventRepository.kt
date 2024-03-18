package com.walletconnect.web3.modal.domain.usecase

import android.content.SharedPreferences

class ConnectionEventRepository(private val sharedPreferences: SharedPreferences) {

    fun saveEvent(name: String, method: String) {
        sharedPreferences.edit().apply {
            putString(WALLET_NAME, name)
            putString(CONNECTION_METHOD, method)
        }.apply()
    }

    fun getEvent(): Pair<String, String> {
        val walletName = sharedPreferences.getString(WALLET_NAME, "") ?: ""
        val connectionMethod = sharedPreferences.getString(CONNECTION_METHOD, "") ?: ""
        return if (walletName.isNotEmpty() && connectionMethod.isNotEmpty()) {
            Pair(walletName, connectionMethod)
        } else {
            Pair("", "")
        }
    }

    fun deleteEvent() {
        sharedPreferences.edit().clear().apply()
    }

    private companion object {
        const val WALLET_NAME = "wallet_name"
        const val CONNECTION_METHOD = "connection_method"
    }
}