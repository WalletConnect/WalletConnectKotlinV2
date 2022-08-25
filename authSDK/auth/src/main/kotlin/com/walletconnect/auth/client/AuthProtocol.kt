package com.walletconnect.auth.client

import com.walletconnect.android_core.common.SDKError
import com.walletconnect.android_core.common.client.Protocol
import com.walletconnect.android_core.common.model.ConnectionState
import com.walletconnect.android_core.common.scope.scope
import com.walletconnect.android_core.di.cryptoModule
import com.walletconnect.android_core.di.networkModule
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.network.data.connection.ConnectionType
import com.walletconnect.auth.BuildConfig
import com.walletconnect.auth.client.mapper.toClient
import com.walletconnect.auth.client.mapper.toClientEvent
import com.walletconnect.auth.client.mapper.toEngineDO
import com.walletconnect.auth.di.commonModule
import com.walletconnect.auth.di.engineModule
import com.walletconnect.auth.di.jsonRpcModule
import com.walletconnect.auth.di.storageModule
import com.walletconnect.auth.engine.domain.AuthEngine
import com.walletconnect.auth.engine.model.EngineDO
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.withLock
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

internal class AuthProtocol : AuthInterface, Protocol() {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var authEngine: AuthEngine

    // TODO: Figure out how to get relay as in Sign in here. Should we keep Relay in the Auth init params?
    internal val relay: RelayConnectionInterface by lazy { wcKoinApp.koin.get() }
    private val serverUrl: String = "wss://relay.walletconnect.com?projectId=2ee94aca5d98e6c05c38bce02bee952a"

    companion object {
        val instance = AuthProtocol()
        private const val STORAGE_SUFFIX = "_auth"
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Auth.Params.Init, onError: (Auth.Model.Error) -> Unit) {
        protocolScope.launch {
            mutex.withLock {
                with(init) {
                    wcKoinApp.run {
                        androidContext(application)
                        modules(
                            commonModule(),
                            cryptoModule(STORAGE_SUFFIX),
                            storageModule(STORAGE_SUFFIX),
                            jsonRpcModule(),
                            engineModule(appMetaData, iss) //idea: Protocol Improvement. Dynamically changing issuer after initialisation
                        )
                    }
                }
                val jwtRepository = wcKoinApp.koin.get<JwtRepository>()
                val jwt = jwtRepository.generateJWT(serverUrl)
                val connectionType = ConnectionType.AUTOMATIC

                //todo: should we allow injecting relayClient? - YES
                wcKoinApp.modules(networkModule(serverUrl, jwt, connectionType, BuildConfig.sdkVersion, null))
                authEngine = wcKoinApp.koin.get()
                authEngine.handleInitializationErrors { error -> onError(Auth.Model.Error(error)) }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun setRequesterDelegate(delegate: AuthInterface.RequesterDelegate) {
        awaitLock {
            authEngine.engineEvent.onEach { event ->
                when (event) {
                    is ConnectionState -> delegate.onConnectionStateChange(event.toClientEvent())
                    is SDKError -> delegate.onError(event.toClientEvent())
                    is EngineDO.Events.onAuthResponse -> delegate.onAuthResponse(event.toClient())
                }
            }.launchIn(scope)
        }
    }

    @Throws(IllegalStateException::class)
    override fun setResponderDelegate(delegate: AuthInterface.ResponderDelegate) {
        awaitLock {
            authEngine.engineEvent.onEach { event ->
                when (event) {
                    is ConnectionState -> delegate.onConnectionStateChange(event.toClientEvent())
                    is SDKError -> delegate.onError(event.toClientEvent())
                    is EngineDO.Events.onAuthRequest -> delegate.onAuthRequest(event.toClient())
                }
            }.launchIn(scope)
        }
    }

    @Throws(IllegalStateException::class)
    override fun pair(pair: Auth.Params.Pair, onError: (Auth.Model.Error) -> Unit) {
        awaitLock {
            try {
                authEngine.pair(pair.uri)
            } catch (error: Exception) {
                onError(Auth.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun request(params: Auth.Params.Request, onPairing: (Auth.Model.Pairing) -> Unit, onError: (Auth.Model.Error) -> Unit) {
        awaitLock {
            try {
                authEngine.request(
                    params.toEngineDO(),
                    onPairing = { proposedSequence -> onPairing(proposedSequence.toClient()) },
                    onFailure = { error -> onError(Auth.Model.Error(error)) }
                )
            } catch (error: Exception) {
                onError(Auth.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun respond(params: Auth.Params.Respond, onError: (Auth.Model.Error) -> Unit) {
        awaitLock {
            try {
                authEngine.respond(params.toEngineDO()) { error -> onError(Auth.Model.Error(error)) }
            } catch (error: Exception) {
                onError(Auth.Model.Error(error))
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun getPendingRequest(): List<Auth.Model.PendingRequest> {
        //todo: add responder check/iss check
        return awaitLock {
            authEngine.getPendingRequests().toClient()
        }
    }

    @Throws(IllegalStateException::class)
    override fun getResponse(params: Auth.Params.RequestId): Auth.Model.Response {
        return awaitLock {
            authEngine.getRequestById(params.id).toClient()
        }
    }

    @Throws(IllegalStateException::class)
    override fun checkEngineInitialization() {
        check(::authEngine.isInitialized) {
            "AuthClient needs to be initialized first using the initialize function"
        }
    }
}