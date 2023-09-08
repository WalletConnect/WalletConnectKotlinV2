package com.walletconnect.web3.modal.domain

import android.content.SharedPreferences

private const val RECENT_WALLET_ID = "w3m_recent_wallet_id"

internal class RecentWalletsRepository(
    private val sharedPreferences: SharedPreferences
) {

    fun saveRecentWalletId(id: String) = sharedPreferences.edit().putString(RECENT_WALLET_ID, id).apply()

    fun getRecentWalletId() = sharedPreferences.getString(RECENT_WALLET_ID, null)
}