package com.walletconnect.push.wallet.client

import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.di.commonUseCasesModule
import com.walletconnect.push.common.di.pushJsonRpcModule
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.wallet.client.mapper.toClient
import com.walletconnect.push.wallet.di.pushStorageModule
import com.walletconnect.push.wallet.di.walletEngineModule
import com.walletconnect.push.wallet.engine.WalletEngine
import com.walletconnect.push.wallet.engine.domain.DecryptMessageUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WalletProtocol : WalletInterface {
    private lateinit var decryptMessageUseCase: DecryptMessageUseCase
    private lateinit var walletEngine: WalletEngine

    companion object {
        val instance = WalletProtocol()
        const val storageSuffix: String = "walletPush"
    }

    override fun initialize(init: Push.Wallet.Params.Init, onError: (Push.Model.Error) -> Unit) {
        try {
            wcKoinApp.modules(
                // TODO: Commented out until we merge PR to handle multiple versions of dependnecy
//                pushCommonModule(),
//                cryptoModule(),
                pushJsonRpcModule(),
                pushStorageModule(storageSuffix),
                walletEngineModule(),
                commonUseCasesModule()
            )

            decryptMessageUseCase = wcKoinApp.koin.get()

            walletEngine = wcKoinApp.koin.get()
            walletEngine.setup()
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: WalletInterface.Delegate) {
        checkEngineInitialization()

        walletEngine.engineEvent.onEach { event ->
            when (event) {
                is EngineDO.PushRequest -> delegate.onPushRequest(event.toClient())
                is EngineDO.PushMessage -> delegate.onPushMessage(event.toClient())
                else -> Unit
            }
        }.launchIn(scope)
    }

    override fun approve(params: Push.Wallet.Params.Approve, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            walletEngine.approve(params.id, onSuccess) { onError(Push.Model.Error(it)) }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun reject(params: Push.Wallet.Params.Reject, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            walletEngine.reject(params.id, params.reason, onSuccess) { onError(Push.Model.Error(it)) }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun getActiveSubscriptions(): Map<String, Push.Model.Subscription> {
        checkEngineInitialization()

        return walletEngine.getListOfActiveSubscriptions().mapValues { (_, subscription) ->
            subscription.toClient()
        }
    }

    override fun delete(params: Push.Wallet.Params.Delete, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            walletEngine.delete(params.topic) { error -> onError(Push.Model.Error(error)) }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (Push.Model.Message) -> Unit, onError: (Push.Model.Error) -> Unit) {
        runCatching { decryptMessageUseCase.invoke(params.topic, params.encryptedMessage) }.fold({ decryptedMsg ->
            onSuccess(Push.Model.Message("", "", "", ""))  // TODO: Need to confirm if decryptedMsg will be broken up to conform to Push.Wallet.Model.Message or will we have different logic
        }, { error ->
            onError(Push.Model.Error(error))
        })
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::walletEngine.isInitialized) {
            "WalletClient needs to be initialized first using the initialize function"
        }
    }
}