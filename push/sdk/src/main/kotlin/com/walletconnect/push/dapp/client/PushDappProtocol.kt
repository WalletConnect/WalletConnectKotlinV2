package com.walletconnect.push.dapp.client

import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.di.pushJsonRpcModule
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.toClient
import com.walletconnect.push.dapp.client.mapper.toClient
import com.walletconnect.push.dapp.client.mapper.toEngineDO
import com.walletconnect.push.dapp.di.dappEngineModule
import com.walletconnect.push.dapp.engine.PushDappEngine
import com.walletconnect.push.wallet.di.pushStorageModule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PushDappProtocol : PushDappInterface {
    private lateinit var pushDappEngine: PushDappEngine

    companion object {
        val instance = PushDappProtocol()
        const val storageSuffix = "DappPush"
    }

    override fun initialize(init: Push.Dapp.Params.Init, onError: (Push.Model.Error) -> Unit) {
        try {
            wcKoinApp.modules(
                pushJsonRpcModule(),
                pushStorageModule(storageSuffix),
                dappEngineModule(),
            )

            pushDappEngine = wcKoinApp.koin.get()
            pushDappEngine.setup()
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: PushDappInterface.Delegate) {
        checkEngineInitialization()

        pushDappEngine.engineEvent
            .onEach { event ->
                when(event) {
                    is EngineDO.PushRequestResponse -> delegate.onPushResponse(event.toClient())
                    is EngineDO.PushRequestRejected -> delegate.onPushRejected(event.toClient())
                    is EngineDO.PushDelete -> delegate.onDelete(event.toClient())
                    is SDKError -> delegate.onError(event.toClient())
                }
            }.launchIn(scope)
    }

    override fun request(params: Push.Dapp.Params.Request, onSuccess: (Push.Dapp.Model.RequestId) -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pushDappEngine.request(params.pairingTopic, params.account,
                { requestId -> onSuccess(Push.Dapp.Model.RequestId(requestId)) },
                { exception -> onError(Push.Model.Error(exception)) }
            )
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun notify(params: Push.Dapp.Params.Notify, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pushDappEngine.notify(params.topic, params.message.toEngineDO()) { exception -> onError(Push.Model.Error(exception)) }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun getActiveSubscriptions(): Map<String, Push.Model.Subscription> {
        checkEngineInitialization()

        return pushDappEngine.getListOfActiveSubscriptions().mapValues { (_, subscription) ->
            subscription.toClient()
        }
    }

    override fun delete(params: Push.Dapp.Params.Delete, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pushDappEngine.delete(params.topic) { error -> onError(Push.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::pushDappEngine.isInitialized) {
            "DappClient needs to be initialized first using the initialize function"
        }
    }
}