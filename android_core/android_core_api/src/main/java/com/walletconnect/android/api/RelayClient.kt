@file:JvmSynthetic

package com.walletconnect.android.api

import android.app.Application
import android.net.Uri
import android.os.Build
import com.walletconnect.android.api.di.*
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import com.walletconnect.foundation.network.BaseRelayClient
import com.walletconnect.foundation.network.data.ConnectionController
import com.walletconnect.foundation.network.model.Relay
import com.walletconnect.foundation.util.Logger
import kotlinx.coroutines.flow.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import java.net.HttpURLConnection

//1. Make RelayClient working as stand alone class with wss connection
//2. Inject it to any instance of sdk client and allow publishing and subscribing
//3. Singleton because we want to force one wss connection accross sdks

object RelayClient : BaseRelayClient(), RelayConnectionInterface {

    private val logger: Logger by lazy { wcKoinApp.koin.get(named(AndroidApiDITags.LOGGER)) }

    private val connectionController: ConnectionController by lazy { wcKoinApp.koin.get(named(AndroidApiDITags.CONNECTION_CONTROLLER)) }
    private val networkState: ConnectivityState by lazy { wcKoinApp.koin.get(named(AndroidApiDITags.CONNECTIVITY_STATE)) }

    private val _isNetworkAvailable: StateFlow<Boolean> by lazy { networkState.isAvailable }
    private val _isWSSConnectionOpened: MutableStateFlow<Boolean> = MutableStateFlow(false)


    fun initialize(relayServerUrl: String, connectionType: ConnectionType, application: Application) {
        println("kobe; RelayClient INIT")
        println("TalhaRelay: $wcKoinApp $test")

        wcKoinApp.run {
            androidContext(application)
            modules(
                commonModule(),
                androidApiCryptoModule()
            )
        }

        val jwtRepository = wcKoinApp.koin.get<JwtRepository>()
        val jwt = jwtRepository.generateJWT(relayServerUrl.strippedUrl())
        val serverUrl = relayServerUrl.addUserAgent("2.0.0") //TODO: how to get sdk version?

        println("kobe; android api network")
        wcKoinApp.modules(androidApiNetworkModule(serverUrl, jwt, connectionType, "2.0.0"))

        println("kobe; getting relay service")
        relayService = wcKoinApp.koin.get(named(AndroidApiDITags.RELAY_SERVICE))

        println("kobe; END init relay client")
    }

    override val isConnectionAvailable: StateFlow<Boolean> by lazy {
        combine(_isWSSConnectionOpened, _isNetworkAvailable) { wss, internet -> wss && internet }
            .stateIn(scope, SharingStarted.Eagerly, false)
    }

    override val initializationErrorsFlow: Flow<WalletConnectException>
        get() = eventsFlow
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

    @get:JvmSynthetic
    private val Throwable.toWalletConnectException: WalletConnectException
        get() =
            when {
                this.message?.contains(HttpURLConnection.HTTP_UNAUTHORIZED.toString()) == true ->
                    ProjectIdDoesNotExistException(this.message)
                this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                    InvalidProjectIdException(this.message)
                else -> GenericException(this.message)
            }

    private fun setIsWSSConnectionOpened(event: Relay.Model.Event) {
        if (event is Relay.Model.Event.OnConnectionOpened<*>) {
            _isWSSConnectionOpened.compareAndSet(expect = false, update = true)
        } else if (event is Relay.Model.Event.OnConnectionClosed || event is Relay.Model.Event.OnConnectionFailed) {
            _isWSSConnectionOpened.compareAndSet(expect = true, update = false)
        }
    }
}

@JvmSynthetic
internal fun String.strippedUrl() = Uri.parse(this).run {
    this@run.scheme + "://" + this@run.authority
}

@JvmSynthetic
internal fun String.addUserAgent(sdkVersion: String): String {
    return Uri.parse(this).buildUpon()
        // TODO: Setup env variable for version and tag. Use env variable here instead of hard coded version
        .appendQueryParameter("ua", """wc-2/kotlin-$sdkVersion/android-${Build.VERSION.RELEASE}""")
        .build()
        .toString()
}