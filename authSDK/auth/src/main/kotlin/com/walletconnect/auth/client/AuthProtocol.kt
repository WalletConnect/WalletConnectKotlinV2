package com.walletconnect.auth.client

import com.walletconnect.utils.Empty
import com.walletconnect.android_core.di.cryptoModule
import com.walletconnect.android_core.di.networkModule
import com.walletconnect.android_core.network.RelayConnectionInterface
import com.walletconnect.android_core.network.data.connection.ConnectionType
import com.walletconnect.auth.BuildConfig
import com.walletconnect.auth.di.commonModule
import com.walletconnect.auth.di.engineModule
import com.walletconnect.auth.di.jsonRpcModule
import com.walletconnect.auth.di.storageModule
import com.walletconnect.auth.engine.domain.AuthEngine
import com.walletconnect.foundation.crypto.data.repository.JwtRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import java.util.concurrent.Executors

internal class AuthProtocol : AuthInterface {
    private val wcKoinApp: KoinApplication = KoinApplication.init()
    private lateinit var authEngine: AuthEngine

    // TODO: Figure out how to get relay as in Sign in here. Should we keep Relay in the Auth init params?
    internal val relay: RelayConnectionInterface by lazy { wcKoinApp.koin.get() }
    private val mutex = Mutex()
    private val signProtocolScope = CoroutineScope(SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    private val serverUrl: String = "wss://relay.walletconnect.com?projectId=2ee94aca5d98e6c05c38bce02bee952a"

    companion object {
        val instance = AuthProtocol()
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Auth.Params.Init, onError: (Auth.Model.Error) -> Unit) {
        signProtocolScope.launch {
            mutex.withLock {
                with(init) {
                    wcKoinApp.run {
                        androidContext(application)
                        modules(
                            commonModule(),
                            cryptoModule(),
                            storageModule(),
                            jsonRpcModule(),
                            engineModule(appMetaData)
                        )
                    }
                }
                val jwtRepository = wcKoinApp.koin.get<JwtRepository>()
                val jwt = jwtRepository.generateJWT(serverUrl)
                val connectionType = ConnectionType.AUTOMATIC

                wcKoinApp.modules(networkModule(serverUrl, jwt, connectionType, BuildConfig.sdkVersion, null)) //todo: should we allow injecting relayClient?
                authEngine = wcKoinApp.koin.get()
                authEngine.handleInitializationErrors { error -> onError(Auth.Model.Error(error)) }
            }
        }
    }

    override fun setRequesterDelegate(delegate: AuthInterface.RequesterDelegate) {
    }

    override fun setResponderDelegate(delegate: AuthInterface.ResponderDelegate) {
    }

    override fun pair(pair: Auth.Params.Pair, onError: (Auth.Model.Error) -> Unit) {
        //TODO("Not yet implemented")
    }

    override fun request(params: Auth.Params.Request) {
        //TODO("Not yet implemented")
    }

    override fun respond(params: Auth.Params.Respond) {
        //TODO("Not yet implemented")
    }

    override fun getPendingRequest(): Map<Int, Auth.Model.PendingRequest> {
        //TODO("Not yet implemented")
        return emptyMap()
    }

    override fun getResponse(params: Auth.Params.RequestId): Auth.Model.Response {
        //TODO("Not yet implemented")
        return Auth.Model.Response.Error(0, String.Empty)
    }
}