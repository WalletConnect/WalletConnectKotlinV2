package com.walletconnect.web3.modal.domain

import android.content.SharedPreferences

private const val SESSION_TOPIC = "session_topic_key"
private const val SELECTED_CHAIN = "selected_chain_key"

internal class SessionRepository(
    private val sharedPreferences: SharedPreferences
) {

    fun saveSessionTopic(topic: String) = sharedPreferences.edit().putString(SESSION_TOPIC, topic).apply()

    fun getSessionTopic() = sharedPreferences.getString(SESSION_TOPIC, null)

    fun deleteSessionTopic() = sharedPreferences.edit().putString(SESSION_TOPIC, null).apply()

    fun saveChainSelection(chain: String) = sharedPreferences.edit().putString(SELECTED_CHAIN, chain).apply()

    fun getSelectedChain() = sharedPreferences.getString(SELECTED_CHAIN, null)
}