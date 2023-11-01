package com.walletconnect.web3.modal.domain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.walletconnect.android.internal.common.scope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

private val SESSION_TOPIC = stringPreferencesKey("session_topic_key")
private val SELECTED_CHAIN = stringPreferencesKey("selected_chain_key")

private val Context.sessionStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "session_store")

internal class SessionRepository(
    private val context: Context
) {

    val sessionTopic: StateFlow<String?> = context.sessionStore.data
        .map { preferences ->
            preferences[SESSION_TOPIC]
        }.stateIn(scope, started = SharingStarted.Lazily, null)

    val selectedChain: StateFlow<String?> = context.sessionStore.data
        .map { preferences ->
            preferences[SELECTED_CHAIN]
        }.stateIn(scope, started = SharingStarted.Lazily, null)

    fun getSelectedChain(): String? = selectedChain.value

    fun getSessionTopic(): String? = sessionTopic.value

    suspend fun saveSessionTopic(topic: String) {
        context.sessionStore.edit { store ->
            store[SESSION_TOPIC] = topic
        }
    }

    suspend fun deleteSessionTopic() {
        context.sessionStore.edit { store ->
            store.remove(SESSION_TOPIC)
        }
    }

    suspend fun saveChainSelection(chain: String) {
        context.sessionStore.edit { store ->
            store[SELECTED_CHAIN] = chain
        }
    }

    suspend fun deleteChainSelection() {
        context.sessionStore.edit { store ->
            store.remove(SESSION_TOPIC)
        }
    }
}