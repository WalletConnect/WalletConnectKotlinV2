@file:JvmSynthetic

package com.walletconnect.android.relay

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.connection.ConnectivityState
import com.walletconnect.android.internal.common.connection.DefaultConnectionLifecycle
import com.walletconnect.android.internal.common.connection.ManualConnectionLifecycle
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.exception.WRONG_CONNECTION_TYPE
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.utils.toWalletConnectException
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.model.Relay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named

class RelayClient(private val koinApp: KoinApplication = wcKoinApp) : BaseRelayClient(), RelayConnectionInterface {
    private val manualConnection: ManualConnectionLifecycle by lazy { koinApp.koin.get(named(AndroidCommonDITags.MANUAL_CONNECTION_LIFECYCLE)) }
    private val defaultConnection: DefaultConnectionLifecycle by lazy { koinApp.koin.get(named(AndroidCommonDITags.DEFAULT_CONNECTION_LIFECYCLE)) }
    private val networkState: ConnectivityState by lazy { koinApp.koin.get(named(AndroidCommonDITags.CONNECTIVITY_STATE)) }
    override val isNetworkAvailable: StateFlow<Boolean?> by lazy { networkState.isAvailable }
    private val _wssConnectionState: MutableStateFlow<WSSConnectionState> = MutableStateFlow(WSSConnectionState.Disconnected.ConnectionClosed())
    override val wssConnectionState: StateFlow<WSSConnectionState> = _wssConnectionState
    private lateinit var connectionType: ConnectionType
    override val onResubscribe: Flow<Any?>
        get() = merge(
            connectionLifecycle.onResume.filter { isResumed -> isResumed != null && isResumed },
            wssConnectionState.filterIsInstance(WSSConnectionState.Connected::class)
        )

    @JvmSynthetic
    fun initialize(connectionType: ConnectionType, onError: (Throwable) -> Unit) {
        this.connectionType = connectionType
        logger = koinApp.koin.get(named(AndroidCommonDITags.LOGGER))
        relayService = koinApp.koin.get(named(AndroidCommonDITags.RELAY_SERVICE))
        connectionLifecycle = if (connectionType == ConnectionType.MANUAL) manualConnection else defaultConnection

        collectConnectionInitializationErrors { error -> onError(error) }
        monitorConnectionState()
        observeResults()
    }

    private fun collectConnectionInitializationErrors(onError: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                eventsFlow
                    .first { event ->
                        if (event is Relay.Model.Event.OnConnectionFailed) {
                            onError(event.throwable.toWalletConnectException)
                        }

                        event !is Relay.Model.Event.OnConnectionOpened<*>
                    }
            }
        }
    }

    private fun monitorConnectionState() {
        eventsFlow
            .onEach { event: Relay.Model.Event -> setIsWSSConnectionOpened(event) }
            .launchIn(scope)
    }

    private fun setIsWSSConnectionOpened(event: Relay.Model.Event) {
        when {
            event is Relay.Model.Event.OnConnectionOpened<*> && _wssConnectionState.value is WSSConnectionState.Disconnected ->
                _wssConnectionState.value = WSSConnectionState.Connected

            event is Relay.Model.Event.OnConnectionFailed && _wssConnectionState.value is WSSConnectionState.Connected ->
                _wssConnectionState.value = WSSConnectionState.Disconnected.ConnectionFailed(event.throwable.toWalletConnectException)

            event is Relay.Model.Event.OnConnectionClosed && _wssConnectionState.value is WSSConnectionState.Connected ->
                _wssConnectionState.value = WSSConnectionState.Disconnected.ConnectionClosed("Connection closed: ${event.shutdownReason.reason} ${event.shutdownReason.code}")
        }
    }

    override fun connect(onError: (Core.Model.Error) -> Unit) {
        when (connectionType) {
            ConnectionType.AUTOMATIC -> onError(Core.Model.Error(IllegalStateException(WRONG_CONNECTION_TYPE)))
            ConnectionType.MANUAL -> manualConnection.connect()
        }
    }

    override fun disconnect(onError: (Core.Model.Error) -> Unit) {
        when (connectionType) {
            ConnectionType.AUTOMATIC -> onError(Core.Model.Error(IllegalStateException(WRONG_CONNECTION_TYPE)))
            ConnectionType.MANUAL -> manualConnection.disconnect()
        }
    }

    override fun restart(onError: (Core.Model.Error) -> Unit) {
        try {
            when (connectionType) {
                ConnectionType.AUTOMATIC -> onError(Core.Model.Error(IllegalStateException(WRONG_CONNECTION_TYPE)))
                ConnectionType.MANUAL -> manualConnection.reconnect()
            }
        } catch (e: Exception) {
            onError(Core.Model.Error(e))
        }
    }

    @Deprecated("This has become deprecate in favor of the onError returning Core.Model.Error", replaceWith = ReplaceWith("this.connect(onErrorModel)"))
    override fun connect(onErrorModel: (Core.Model.Error) -> Unit, onError: (String) -> Unit) {
        when (connectionType) {
            ConnectionType.AUTOMATIC -> onError(WRONG_CONNECTION_TYPE)
            ConnectionType.MANUAL -> manualConnection.connect()
        }
    }

    @Deprecated("This has become deprecate in favor of the onError returning Core.Model.Error", replaceWith = ReplaceWith("this.disconnect(onErrorModel)"))
    override fun disconnect(onErrorModel: (Core.Model.Error) -> Unit, onError: (String) -> Unit) {
        when (connectionType) {
            ConnectionType.AUTOMATIC -> onError(WRONG_CONNECTION_TYPE)
            ConnectionType.MANUAL -> manualConnection.disconnect()
        }
    }
}