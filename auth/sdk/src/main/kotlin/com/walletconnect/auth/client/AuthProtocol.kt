@file:JvmSynthetic

package com.walletconnect.auth.client

import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.impl.common.model.ConnectionState
import com.walletconnect.android.impl.di.cryptoModule
import com.walletconnect.android.impl.utils.Logger
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.auth.client.mapper.toClient
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.model.Events
import com.walletconnect.auth.di.commonModule
import com.walletconnect.auth.di.engineModule
import com.walletconnect.auth.di.jsonRpcModule
import com.walletconnect.auth.di.storageModule
import com.walletconnect.auth.engine.domain.AuthEngine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class AuthProtocol : AuthInterface {
    private lateinit var authEngine: AuthEngine

    companion object {
        val instance = AuthProtocol()
    }

    @Throws(IllegalStateException::class)
    override fun initialize(init: Auth.Params.Init, onError: (Auth.Model.Error) -> Unit) {
        Logger.init()

        with(init) {
            wcKoinApp.modules(
                commonModule(),
                cryptoModule(),
                jsonRpcModule(),
                storageModule(),
                engineModule(iss)
            )
        }

        authEngine = wcKoinApp.koin.get()
        authEngine.handleInitializationErrors { error -> onError(Auth.Model.Error(error)) }
    }

    @Throws(IllegalStateException::class)
    override fun setRequesterDelegate(delegate: AuthInterface.RequesterDelegate) {
        checkEngineInitialization()
        authEngine.engineEvent.onEach { event ->
            when (event) {
                is ConnectionState -> delegate.onConnectionStateChange(event.toClient())
                is SDKError -> delegate.onError(event.toClient())
                is Events.OnAuthResponse -> delegate.onAuthResponse(event.toClient())
            }
        }.launchIn(scope)
    }

    @Throws(IllegalStateException::class)
    override fun setResponderDelegate(delegate: AuthInterface.ResponderDelegate) {
        checkEngineInitialization()
        authEngine.engineEvent.onEach { event ->
            when (event) {
                is ConnectionState -> delegate.onConnectionStateChange(event.toClient())
                is SDKError -> delegate.onError(event.toClient())
                is Events.OnAuthRequest -> delegate.onAuthRequest(event.toClient())
            }
        }.launchIn(scope)
    }

    @Throws(IllegalStateException::class)
    override fun request(params: Auth.Params.Request, onPairing: (Auth.Model.Pairing) -> Unit, onError: (Auth.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            authEngine.request(
                params.toCommon(),
                onPairing = { uri -> onPairing(uri.toClient()) },
                onFailure = { error -> onError(Auth.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Auth.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun respond(params: Auth.Params.Respond, onError: (Auth.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            authEngine.respond(params.toCommon()) { error -> onError(Auth.Model.Error(error)) }
        } catch (error: Exception) {
            onError(Auth.Model.Error(error))
        }
    }

    @Throws(Exception::class)
    override fun getPendingRequest(): List<Auth.Model.PendingRequest> {
        checkEngineInitialization()

        return authEngine.getPendingRequests().toClient()
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::authEngine.isInitialized) {
            "AuthClient needs to be initialized first using the initialize function"
        }
    }
}