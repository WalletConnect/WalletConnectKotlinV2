package com.walletconnect.web3.modal.domain

import android.content.SharedPreferences
import androidx.core.content.edit

private const val SESSION_TOPIC = "session_topic_key"
private const val SELECTED_CHAIN = "selected_chain_key"

internal class SessionRepository(
    private val sharedPreferences: SharedPreferences
) {

    fun saveSessionTopic(topic: String) = sharedPreferences.edit {
        putString(SESSION_TOPIC, topic)
    }

    fun getSessionTopic() = sharedPreferences.getString(SESSION_TOPIC, null)

    fun deleteSessionTopic() = sharedPreferences.edit {
        putString(SESSION_TOPIC, null)
    }

    fun saveChainSelection(chain: String) = sharedPreferences.edit {
        putString(SELECTED_CHAIN, chain)
    }

    fun getSelectedChain() = sharedPreferences.getString(SELECTED_CHAIN, null)

    fun deleteChainSelection() = sharedPreferences.edit {
        putString(SELECTED_CHAIN, null)
    }
}