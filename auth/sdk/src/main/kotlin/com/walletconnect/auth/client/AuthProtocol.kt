@file:JvmSynthetic

package com.walletconnect.auth.client

import com.walletconnect.android.internal.common.model.ConnectionState
import com.walletconnect.android.internal.common.model.Expiry
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.auth.client.mapper.toClient
import com.walletconnect.auth.client.mapper.toCommon
import com.walletconnect.auth.common.model.Events
import com.walletconnect.auth.di.commonModule
import com.walletconnect.auth.di.engineModule
import com.walletconnect.auth.di.jsonRpcModule
import com.walletconnect.auth.engine.domain.AuthEngine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.KoinApplication

internal class AuthProtocol(private val koinApp: KoinApplication = wcKoinApp) : AuthInterface {
    private lateinit var authEngine: AuthEngine

    companion object {
        val instance = AuthProtocol()
    }

    @Throws(IllegalStateException::class)
    override fun initialize(params: Auth.Params.Init, onSuccess: () -> Unit, onError: (Auth.Model.Error) -> Unit) {
        try {
            koinApp.modules(
                jsonRpcModule(),
                engineModule(),
                commonModule()
            )

            authEngine = koinApp.koin.get()
            authEngine.setup()
            onSuccess()
        } catch (e: Exception) {
            onError(Auth.Model.Error(e))
        }
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
    override fun request(params: Auth.Params.Request, onSuccess: () -> Unit, onError: (Auth.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            val expiry = params.expiry?.run { Expiry(this) }
            authEngine.request(params.toCommon(), expiry, params.topic,
                onSuccess = onSuccess,
                onFailure = { error -> onError(Auth.Model.Error(error)) }
            )
        } catch (error: Exception) {
            onError(Auth.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun respond(params: Auth.Params.Respond, onSuccess: (Auth.Params.Respond) -> Unit, onError: (Auth.Model.Error) -> Unit) {
        checkEngineInitialization()
        try {
            authEngine.respond(params.toCommon(), { onSuccess(params) }, { error -> onError(Auth.Model.Error(error)) })
        } catch (error: Exception) {
            onError(Auth.Model.Error(error))
        }
    }

    @Throws(IllegalStateException::class)
    override fun formatMessage(params: Auth.Params.FormatMessage): String? {
        checkEngineInitialization()

        return try {
            authEngine.formatMessage(params.payloadParams.toCommon(), params.issuer)
        } catch (error: Exception) {
            null
        }
    }

    @Throws(IllegalStateException::class)
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