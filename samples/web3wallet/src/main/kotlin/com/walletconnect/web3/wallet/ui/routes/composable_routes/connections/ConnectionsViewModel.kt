package com.walletconnect.web3.wallet.ui.routes.composable_routes.connections

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.Core
import com.walletconnect.web3.wallet.domain.WCDelegate
import com.walletconnect.web3.wallet.ui.CoreEvent
import com.walletconnect.web3.wallet.ui.NoAction
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

class ConnectionsViewModel : ViewModel() {

    private var _refreshFlow: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 0, extraBufferCapacity = 1, BufferOverflow.DROP_OLDEST)
    private var refreshFlow: SharedFlow<Unit> = _refreshFlow.asSharedFlow()
    private val signConnectionsFlow = merge(WCDelegate.walletEvents, refreshFlow).map {
        Log.d("Web3Wallet", "signConnectionsFlow: $it")
        getLatestActiveSignSessions()
    }

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

    private fun refreshCurrentConnectionUI() {
        currentConnectionUI.value = getConnectionUI()
    }

    val coreEvents = WCDelegate.coreEvents.map { wcEvent ->
        when (wcEvent) {
            is Core.Model.DeletedPairing -> CoreEvent.Disconnect
            else -> NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

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