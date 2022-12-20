package com.walletconnect.push.dapp.client

import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.di.commonUseCasesModule
import com.walletconnect.push.common.domain.GetListOfSubscriptionsUseCase
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.dapp.client.mapper.toClientPushRequest
import com.walletconnect.push.dapp.client.mapper.toClientPushResponse
import com.walletconnect.push.dapp.client.mapper.toEngineDO
import com.walletconnect.push.dapp.di.dappEngineModule
import com.walletconnect.push.dapp.engine.DappEngine
import com.walletconnect.push.common.di.pushJsonRpcModule
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class DappProtocol : DappInterface {
    private lateinit var getListOfSubscriptionsUseCase: GetListOfSubscriptionsUseCase
    private lateinit var dappEngine: DappEngine

    companion object {
        val instance = DappProtocol()
    }

    override fun initialize(init: Push.Dapp.Params.Init, onError: (Push.Dapp.Model.Error) -> Unit) {
        try {
            wcKoinApp.modules(
//                pushCommonModule(),
//                cryptoModule(),
                pushJsonRpcModule(),
//                pushStorageModule(WalletProtocol.storageSuffix),
                dappEngineModule(),
                commonUseCasesModule()
            )

            getListOfSubscriptionsUseCase = wcKoinApp.koin.get()

            dappEngine = wcKoinApp.koin.get()
            dappEngine.setup()
        } catch (e: Exception) {
            onError(Push.Dapp.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: DappInterface.Delegate) {
        checkEngineInitialization()

        dappEngine.engineEvent
            .filterIsInstance<EngineDO.PushRequestResponse>()
            .onEach { event ->
                delegate.onPushResponse(event.toClientPushResponse())
            }.launchIn(scope)
    }

    override fun request(params: Push.Dapp.Params.Request, onSuccess: (Push.Dapp.Model.RequestId) -> Unit, onError: (Push.Dapp.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            dappEngine.request(params.pairingTopic, params.account,
                { requestId -> onSuccess(Push.Dapp.Model.RequestId(requestId)) },
                { exception -> onError(Push.Dapp.Model.Error(exception)) }
            )
        } catch (e: Exception) {
            onError(Push.Dapp.Model.Error(e))
        }
    }

    override fun notify(params: Push.Dapp.Params.Notify, onError: (Push.Dapp.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            dappEngine.notify(params.topic, params.message.toEngineDO()) { exception -> onError(Push.Dapp.Model.Error(exception)) }
        } catch (e: Exception) {
            onError(Push.Dapp.Model.Error(e))
        }
    }

    override fun getActiveSubscriptions(): Map<String, Push.Dapp.Model.Subscription> {
        checkEngineInitialization()

        return getListOfSubscriptionsUseCase.invoke().mapValues { (_, subscription) ->
            subscription.toClientPushRequest()
        }
    }

    override fun delete(params: Push.Dapp.Params.Delete, onError: (Push.Dapp.Model.Error) -> Unit) {
        checkEngineInitialization()

        TODO("Not yet implemented")
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::dappEngine.isInitialized) {
            "DappClient needs to be initialized first using the initialize function"
        }
    }
}