package com.walletconnect.web3.modal.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.squareup.moshi.Moshi
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val SESSION = stringPreferencesKey("session_key")

internal class SessionRepository(
    private val sessionStore: DataStore<Preferences>,
    moshi: Moshi
) {
    private val adapter = moshi.adapter(Session::class.java)

    val session: Flow<Session?> = sessionStore.data
        .map { preferences ->
            preferences[SESSION]?.let { adapter.fromJson(it) }
        }

    suspend fun getSession(): Session? = session.first()

    suspend fun saveSession(session: Session) {
        sessionStore.edit { store -> store[SESSION] = adapter.toJson(session) }
    }

    suspend fun deleteSession() {
        sessionStore.edit { store -> store.remove(SESSION) }
    }

    suspend fun updateChainSelection(chain: String) {
        sessionStore.edit { store ->
            val updatedSession = when (val session = store[SESSION]?.let { adapter.fromJson(it) }) {
                is Session.Coinbase -> session.copy(chain = chain)
                is Session.WalletConnect -> session.copy(chain = chain)
                null -> null
            }
            Web3Modal.selectedChain = Web3Modal.chains.find { it.id == chain }
            store[SESSION] = adapter.toJson(updatedSession)
        }
    }
}
