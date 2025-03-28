package com.walletconnect.sample.wallet.ui.routes.composable_routes.connections

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.sample.wallet.domain.ACCOUNTS_1_EIP155_ADDRESS
import com.walletconnect.sample.wallet.domain.ACCOUNTS_2_EIP155_ADDRESS
import com.walletconnect.sample.wallet.domain.WCDelegate
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

class ConnectionsViewModel : ViewModel() {
    private var _refreshFlow: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 0, extraBufferCapacity = 1, BufferOverflow.DROP_OLDEST)
    private var refreshFlow: SharedFlow<Unit> = _refreshFlow.asSharedFlow()
    private val signConnectionsFlow = merge(WCDelegate.walletEvents, refreshFlow).map {
        Log.d("Web3Wallet", "signConnectionsFlow: $it")
        getLatestActiveSignSessions()
    }
    var displayedAccounts: List<String> = emptyList()

    var currentConnectionId: Int? = null
        set(value) {
            field = value
            refreshCurrentConnectionUI()
        }

    private fun getConnectionUI(): ConnectionUI? = connections.value.firstOrNull { it.id == currentConnectionId }

    val connections: StateFlow<List<ConnectionUI>> =
        signConnectionsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, getLatestActiveSignSessions())

    val currentConnectionUI: MutableState<ConnectionUI?> = mutableStateOf(getConnectionUI())

    // Refreshes connections list from Web3Wallet
    fun refreshConnections() {
        val res = _refreshFlow.tryEmit(Unit)
        Log.e("Web3Wallet", "refreshConnections $res")
    }

    private var areNewAccounts: Boolean = true

    fun getAccountsToChange(): String {
        return if (areNewAccounts) {
            areNewAccounts = false
            "[\"${"eip155:1:$ACCOUNTS_2_EIP155_ADDRESS"}\",\"${"eip155:137:$ACCOUNTS_2_EIP155_ADDRESS"}\",\"${"eip155:56:$ACCOUNTS_2_EIP155_ADDRESS"}\"]"
        } else {
            areNewAccounts = true
            "[\"${"eip155:1:$ACCOUNTS_1_EIP155_ADDRESS"}\",\"${"eip155:137:${ACCOUNTS_1_EIP155_ADDRESS}"}\",\"${"eip155:56:$ACCOUNTS_1_EIP155_ADDRESS"}\"]"
        }
    }

    private fun refreshCurrentConnectionUI() {
        currentConnectionUI.value = getConnectionUI()
    }

    private fun getLatestActiveSignSessions(): List<ConnectionUI> {
        return try {
            Web3Wallet.getListOfActiveSessions().filter { wcSession ->
                wcSession.metaData != null
            }.mapIndexed { index, wcSession ->
                ConnectionUI(
                    icon = wcSession.metaData?.icons?.firstOrNull(),
                    name = wcSession.metaData!!.name.takeIf { it.isNotBlank() } ?: "Dapp",
                    uri = wcSession.metaData!!.url.takeIf { it.isNotBlank() } ?: "Not provided",
                    id = index,
                    type = ConnectionType.Sign(topic = wcSession.topic, namespaces = wcSession.namespaces),
                )
            }
        } catch (e: Exception) {
            return emptyList()
        }
    }
}