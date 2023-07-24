package com.walletconnect.wcmodal.domain

import android.content.SharedPreferences

private const val RECENT_WALLET_ID = "recent_wallet_id"

class WalletConnectModalStorage(
    private val sharedPreferences: SharedPreferences
) {

    fun saveRecentWalletId(id: String) = sharedPreferences.edit().putString(RECENT_WALLET_ID, id).apply()

    fun getRecentWalletId() = sharedPreferences.getString(RECENT_WALLET_ID, null)
}
