@file:JvmSynthetic

package com.walletconnect.android.relay

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.connection.ConnectivityState
import com.walletconnect.android.internal.common.di.AndroidCommonDITags
import com.walletconnect.android.internal.common.exception.WRONG_CONNECTION_TYPE
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.utils.toWalletConnectException
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.data.ConnectionController
import com.walletconnect.foundation.network.model.Relay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named

class RelayClient(private val koinApp: KoinApplication = wcKoinApp) : BaseRelayClient(), RelayConnectionInterface {
    private val connectionController: ConnectionController by lazy { koinApp.koin.get(named(AndroidCommonDITags.CONNECTION_CONTROLLER)) }
    private val networkState: ConnectivityState by lazy { koinApp.koin.get(named(AndroidCommonDITags.CONNECTIVITY_STATE)) }
    override val isNetworkAvailable: StateFlow<Boolean?> by lazy { networkState.isAvailable }
    private val _wssConnectionState: MutableStateFlow<WSSConnectionState> = MutableStateFlow(WSSConnectionState.Disconnected())
    override val wssConnectionState: StateFlow<WSSConnectionState> = _wssConnectionState

    @JvmSynthetic
    fun initialize(onError: (Throwable) -> Unit) {
        logger = koinApp.koin.get(named(AndroidCommonDITags.LOGGER))
        relayService = koinApp.koin.get(named(AndroidCommonDITags.RELAY_SERVICE))
        collectConnectionInitializationErrors { error -> onError(error) }
        monitorConnectionState()
        observeResults()
    }

    private fun collectConnectionInitializationErrors(onError: (Throwable) -> Unit) {
        scope.launch {
            supervisorScope {
                eventsFlow
                    .takeWhile { event ->
                        if (event is Relay.Model.Event.OnConnectionFailed) {
                            onError(event.throwable.toWalletConnectException)
                        }

                        event !is Relay.Model.Event.OnConnectionOpened<*>
                }.collect()
            }
        }
    }

    private fun monitorConnectionState() {
        eventsFlow
            .onEach { event: Relay.Model.Event ->
                println("kobe: Relay connection event: $event")
                setIsWSSConnectionOpened(event)
            }
            .launchIn(scope)
    }

    private fun setIsWSSConnectionOpened(event: Relay.Model.Event) {
        when (event) {
            is Relay.Model.Event.OnConnectionOpened<*> -> {
                if (_wssConnectionState.value is WSSConnectionState.Disconnected) {
                    _wssConnectionState.value = WSSConnectionState.Connected
                }
            }

            is Relay.Model.Event.OnConnectionFailed -> {
                if (_wssConnectionState.value is WSSConnectionState.Connected) {
                    _wssConnectionState.value = WSSConnectionState.Disconnected(event.throwable)
                }
            }

            is Relay.Model.Event.OnConnectionClosed -> {
                if (_wssConnectionState.value is WSSConnectionState.Connected) {
                    _wssConnectionState.value = WSSConnectionState.Disconnected(Throwable("Connection closed: ${event.shutdownReason.reason} ${event.shutdownReason.code}"))
                }
            }

            else -> Unit
        }
    }

    override fun connect(onErrorModel: (Core.Model.Error) -> Unit, onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> (connectionController as ConnectionController.Manual).connect()
        }
    }

    override fun connect(onError: (Core.Model.Error) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(Core.Model.Error(IllegalStateException(WRONG_CONNECTION_TYPE)))
            is ConnectionController.Manual -> (connectionController as ConnectionController.Manual).connect()
        }
    }

    override fun disconnect(onErrorModel: (Core.Model.Error) -> Unit, onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> (connectionController as ConnectionController.Manual).disconnect()
        }
    }

    override fun disconnect(onError: (Core.Model.Error) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(Core.Model.Error(IllegalStateException(WRONG_CONNECTION_TYPE)))
            is ConnectionController.Manual -> (connectionController as ConnectionController.Manual).disconnect()
        }
    }
}