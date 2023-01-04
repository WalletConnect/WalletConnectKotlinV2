package com.walletconnect.push.wallet.client

import android.util.Log
import com.walletconnect.android.impl.common.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.di.pushJsonRpcModule
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.toClient
import com.walletconnect.push.wallet.client.mapper.toClient
import com.walletconnect.push.wallet.di.pushStorageModule
import com.walletconnect.push.wallet.di.walletEngineModule
import com.walletconnect.push.wallet.engine.PushWalletEngine
import com.walletconnect.push.wallet.engine.domain.DecryptMessageUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PushWalletProtocol : PushWalletInterface {
    private lateinit var decryptMessageUseCase: DecryptMessageUseCase
    private lateinit var pushWalletEngine: PushWalletEngine

    companion object {
        val instance = PushWalletProtocol()
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
            )

            decryptMessageUseCase = wcKoinApp.koin.get()

            pushWalletEngine = wcKoinApp.koin.get()
            pushWalletEngine.setup()
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: PushWalletInterface.Delegate) {
        checkEngineInitialization()

        pushWalletEngine.engineEvent.onEach { event ->
            when (event) {
                is EngineDO.PushRequest -> delegate.onPushRequest(event.toClient())
                is EngineDO.PushMessage -> delegate.onPushMessage(event.toClient())
                is SDKError -> delegate.onError(event.toClient())
            }
        }.launchIn(scope)
    }

    override fun approve(params: Push.Wallet.Params.Approve, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pushWalletEngine.approve(params.id, onSuccess) { onError(Push.Model.Error(it)) }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun reject(params: Push.Wallet.Params.Reject, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pushWalletEngine.reject(params.id, params.reason, onSuccess) { onError(Push.Model.Error(it)) }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun getActiveSubscriptions(): Map<String, Push.Model.Subscription> {
        checkEngineInitialization()

        return pushWalletEngine.getListOfActiveSubscriptions().mapValues { (_, subscription) ->
            subscription.toClient()
        }
    }

    override fun delete(params: Push.Wallet.Params.Delete, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pushWalletEngine.delete(params.topic) { error -> onError(Push.Model.Error(error)) }
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
        check(::pushWalletEngine.isInitialized) {
            "WalletClient needs to be initialized first using the initialize function"
        }
    }
}