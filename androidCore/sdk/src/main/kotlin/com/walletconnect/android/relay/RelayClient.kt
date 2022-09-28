@file:JvmSynthetic

package com.walletconnect.android.relay

import android.app.Application
import com.walletconnect.android.common.connection.ConnectivityState
import com.walletconnect.android.common.di.AndroidCommonDITags
import com.walletconnect.android.common.di.androidApiNetworkModule
import com.walletconnect.android.common.exception.WRONG_CONNECTION_TYPE
import com.walletconnect.android.common.exception.WalletConnectException
import com.walletconnect.android.common.relay.RelayConnectionInterface
import com.walletconnect.android.common.scope
import com.walletconnect.android.common.wcKoinApp
import com.walletconnect.android.connection.ConnectionType
import com.walletconnect.android.utils.*
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.data.ConnectionController
import com.walletconnect.foundation.network.model.Relay
import kotlinx.coroutines.flow.*
import org.koin.core.qualifier.named

internal object RelayClient : BaseRelayClient(), RelayConnectionInterface {
    private val connectionController: ConnectionController by lazy { wcKoinApp.koin.get(named(AndroidCommonDITags.CONNECTION_CONTROLLER)) }
    private val networkState: ConnectivityState by lazy { wcKoinApp.koin.get(named(AndroidCommonDITags.CONNECTIVITY_STATE)) }
    private val isNetworkAvailable: StateFlow<Boolean> by lazy { networkState.isAvailable }
    private val isWSSConnectionOpened: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private const val sdkVersion: String = "2.0.0" //TODO: get android core sdk version

    fun initialize(relayServerUrl: String, connectionType: ConnectionType, application: Application) {
        require(relayServerUrl.isValidRelayServerUrl()) { "Check the schema and projectId parameter of the Server Url" }

        logger = wcKoinApp.koin.get(named(AndroidCommonDITags.LOGGER))
        val jwtRepository = wcKoinApp.koin.get<JwtRepository>()
        val jwt = jwtRepository.generateJWT(relayServerUrl.strippedUrl())
        val serverUrl = relayServerUrl.addUserAgent(sdkVersion)

        wcKoinApp.modules(androidApiNetworkModule(relay = this, serverUrl, jwt, connectionType.toCommonConnectionType(), sdkVersion))
        relayService = wcKoinApp.koin.get(named(AndroidCommonDITags.RELAY_SERVICE))
    }

    override val isConnectionAvailable: StateFlow<Boolean> by lazy {
        combine(isWSSConnectionOpened, isNetworkAvailable) { wss, internet -> wss && internet }
            .stateIn(scope, SharingStarted.Eagerly, false)
    }

    override val initializationErrorsFlow: Flow<WalletConnectException>
        get() =
            eventsFlow
                .onEach { event: Relay.Model.Event ->
                    logger.log("$event")
                    setIsWSSConnectionOpened(event)
                }
                .filterIsInstance<Relay.Model.Event.OnConnectionFailed>()
                .map { error -> error.throwable.toWalletConnectException }

    override fun connect(onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> (connectionController as ConnectionController.Manual).connect()
        }
    }

    override fun disconnect(onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> (connectionController as ConnectionController.Manual).disconnect()
        }
    }

    private fun setIsWSSConnectionOpened(event: Relay.Model.Event) {
        if (event is Relay.Model.Event.OnConnectionOpened<*>) {
            isWSSConnectionOpened.compareAndSet(expect = false, update = true)
        } else if (event is Relay.Model.Event.OnConnectionClosed || event is Relay.Model.Event.OnConnectionFailed) {
            isWSSConnectionOpened.compareAndSet(expect = true, update = false)
        }
    }
}