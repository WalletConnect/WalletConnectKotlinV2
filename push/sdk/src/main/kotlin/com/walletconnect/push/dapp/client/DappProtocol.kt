package com.walletconnect.push.dapp.client

import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.di.commonUseCasesModule
import com.walletconnect.push.common.di.pushJsonRpcModule
import com.walletconnect.push.common.domain.GetListOfSubscriptionsUseCase
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.dapp.client.mapper.toClient
import com.walletconnect.push.dapp.client.mapper.toEngineDO
import com.walletconnect.push.dapp.di.dappEngineModule
import com.walletconnect.push.dapp.engine.DappEngine
import com.walletconnect.push.wallet.di.pushStorageModule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class DappProtocol : DappInterface {
    private lateinit var getListOfSubscriptionsUseCase: GetListOfSubscriptionsUseCase
    private lateinit var dappEngine: DappEngine

    companion object {
        val instance = DappProtocol()
        const val storageSuffix = "dappPush"
    }

    override fun initialize(init: Push.Dapp.Params.Init, onError: (Push.Model.Error) -> Unit) {
        try {
            wcKoinApp.modules(
                // TODO: Commented out until we merge PR to handle multiple versions of dependnecy
//                pushCommonModule(),
//                cryptoModule(),
                pushJsonRpcModule(),
                pushStorageModule(storageSuffix),
                dappEngineModule(),
                commonUseCasesModule()
            )

            getListOfSubscriptionsUseCase = wcKoinApp.koin.get()

            dappEngine = wcKoinApp.koin.get()
            dappEngine.setup()
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: DappInterface.Delegate) {
        checkEngineInitialization()

        dappEngine.engineEvent
            .onEach { event ->
                when(event) {
                    is EngineDO.PushRequestResponse -> delegate.onPushResponse(event.toClient())
                    is EngineDO.PushRequestRejected -> delegate.onPushRejected(event.toClient())
                    is EngineDO.PushDelete -> delegate.onDelete(event.toClient())
                }
            }.launchIn(scope)
    }

    override fun request(params: Push.Dapp.Params.Request, onSuccess: (Push.Dapp.Model.RequestId) -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            dappEngine.request(params.pairingTopic, params.account,
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
            dappEngine.notify(params.topic, params.message.toEngineDO()) { exception -> onError(Push.Model.Error(exception)) }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun getActiveSubscriptions(): Map<String, Push.Model.Subscription> {
        checkEngineInitialization()

        return dappEngine.getListOfActiveSubscriptions().mapValues { (_, subscription) ->
            subscription.toClient()
        }
    }

    override fun delete(params: Push.Dapp.Params.Delete, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            dappEngine.delete(params.topic) { error -> onError(Push.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::dappEngine.isInitialized) {
            "DappClient needs to be initialized first using the initialize function"
        }
    }
}