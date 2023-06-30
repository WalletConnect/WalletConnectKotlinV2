package com.walletconnect.push.dapp.client

import com.walletconnect.android.internal.common.di.DatabaseConfig
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.di.commonModule
import com.walletconnect.push.common.di.pushEngineUseCaseModules
import com.walletconnect.push.common.di.pushJsonRpcModule
import com.walletconnect.push.common.di.pushStorageModule
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.toClient
import com.walletconnect.push.common.model.toDappClient
import com.walletconnect.push.common.model.toEngineDO
import com.walletconnect.push.dapp.di.castModule
import com.walletconnect.push.dapp.di.dappEngineModule
import com.walletconnect.push.dapp.engine.PushDappEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinApplication

internal class PushDappProtocol(private val koinApp: KoinApplication = wcKoinApp) : PushDappInterface {
    private lateinit var pushDappEngine: PushDappEngine

    companion object {
        val instance = PushDappProtocol()
    }

    override fun initialize(init: Push.Dapp.Params.Init, onError: (Push.Model.Error) -> Unit) {
        try {
            koinApp.modules(
                pushJsonRpcModule(),
                pushStorageModule(koinApp.koin.get<DatabaseConfig>().PUSH_DAPP_SDK_DB_NAME),
                dappEngineModule(),
                castModule(init.castUrl),
                commonModule(),
                pushEngineUseCaseModules()
            )

            pushDappEngine = koinApp.koin.get()
            pushDappEngine.setup()
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: PushDappInterface.Delegate) {
        checkEngineInitialization()

        pushDappEngine.engineEvent
            .onEach { event ->
                when (event) {
                    is EngineDO.PushRequestResponse -> delegate.onPushResponse(event.toDappClient())
                    is EngineDO.PushRequestRejected -> delegate.onPushRejected(event.toDappClient())
                    is EngineDO.PushDelete -> delegate.onDelete(event.toDappClient())
                    is SDKError -> delegate.onError(event.toClient())
                }
            }.launchIn(scope)
    }

    override fun propose(params: Push.Dapp.Params.Propose, onSuccess: (Push.Dapp.Model.RequestId) -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch(Dispatchers.IO) {
            try {
                pushDappEngine.propose(
                    params.account,
                    params.scope,
                    params.pairingTopic,
                    { requestId -> onSuccess(Push.Dapp.Model.RequestId(requestId)) },
                    { exception -> onError(Push.Model.Error(exception)) }
                )
            } catch (e: Exception) {
                onError(Push.Model.Error(e))
            }
        }
    }

    override fun notify(params: Push.Dapp.Params.Notify, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch(Dispatchers.IO) {
            try {
                pushDappEngine.notify(params.topic, params.message.toEngineDO()) { exception -> onError(Push.Model.Error(exception)) }
            } catch (e: Exception) {
                onError(Push.Model.Error(e))
            }
        }
    }

    override fun getActiveSubscriptions(): Map<String, Push.Model.Subscription> {
        checkEngineInitialization()

        return runBlocking {
            pushDappEngine.getListOfActiveSubscriptions().mapValues { (_, subscription) ->
                subscription.toDappClient()
            }
        }
    }

    override fun deleteSubscription(params: Push.Dapp.Params.Delete, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch(Dispatchers.IO) {
            try {
                pushDappEngine.delete(params.topic) { error -> onError(Push.Model.Error(error)) }
            } catch (e: Exception) {
                onError(Push.Model.Error(e))
            }
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::pushDappEngine.isInitialized) {
            "DappClient needs to be initialized first using the initialize function"
        }
    }
}