package com.walletconnect.push.wallet.client

import com.walletconnect.android.impl.di.cryptoModule
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.wallet.client.mapper.toClientPushRequest
import com.walletconnect.push.wallet.di.pushCommonModule
import com.walletconnect.push.wallet.di.pushJsonRpcModule
import com.walletconnect.push.wallet.di.pushStorageModule
import com.walletconnect.push.wallet.di.walletEngineModule
import com.walletconnect.push.wallet.engine.domain.DecryptMessageUseCase
import com.walletconnect.push.wallet.engine.domain.WalletEngine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WalletProtocol : WalletInterface {
    private lateinit var decryptMessageUseCase: DecryptMessageUseCase
    private lateinit var pushEngine: WalletEngine

    companion object {
        val instance = WalletProtocol()
        const val storageSuffix: String = "push"
    }

    override fun initialize(onError: (Push.Wallet.Model.Error) -> Unit) {
        try {
            wcKoinApp.modules(
                pushCommonModule(),
                cryptoModule(),
                pushJsonRpcModule(),
                pushStorageModule(storageSuffix),
                walletEngineModule()
            )

            decryptMessageUseCase = wcKoinApp.koin.get()

            pushEngine = wcKoinApp.koin.get()
            pushEngine.setup()
        } catch (e: Exception) {
            onError(Push.Wallet.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: WalletInterface.Delegate) {
        checkEngineInitialization()

        pushEngine.engineEvent.onEach { event ->
            when (event) {
                is EngineDO.PushRequest -> delegate.onPushRequest(event.toClientPushRequest())
                is EngineDO.Message -> delegate.onPushMessage(event.toClientPushRequest())
            }
        }.launchIn(scope)
    }

    override fun approve(params: Push.Wallet.Params.Approve, onSuccess: (Boolean) -> Unit, onError: (Push.Wallet.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            // TODO: Find out if it's better to pass the publicKey instead of the ID
            pushEngine.approve(/*params.id*/"", onSuccess) { onError(Push.Wallet.Model.Error(it)) }
        } catch (e: Exception) {
            onError(Push.Wallet.Model.Error(e))
        }
    }

    override fun reject(params: Push.Wallet.Params.Reject, onSuccess: (Boolean) -> Unit, onError: (Push.Wallet.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            // TODO: Find out if it's better to pass the publicKey instead of the ID
            pushEngine.reject(/*params.id*/"", params.reason, onSuccess) { onError(Push.Wallet.Model.Error(it)) }
        } catch (e: Exception) {
            onError(Push.Wallet.Model.Error(e))
        }
    }

    override fun getActiveSubscriptions(): Map<String, Push.Wallet.Model.Subscription> {
        return pushEngine.getListOfSubscriptions().mapValues { (_, subscription) ->
            subscription.toClientPushRequest()
        }
    }

    override fun delete(params: Push.Wallet.Params.Delete) {
        // TODO: This is still being decided on
    }

    override fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (Push.Wallet.Model.Message) -> Unit, onError: (Push.Wallet.Model.Error) -> Unit) {
        runCatching { decryptMessageUseCase(params.topic, params.encryptedMessage) }.fold({ decryptedMsg ->
            onSuccess(Push.Wallet.Model.Message("", "", "", ""))  // TODO: Need to confirm if decryptedMsg will be broken up to conform to Push.Wallet.Model.Message or will we have different logic
        }, {
            onError(Push.Wallet.Model.Error(it))
        })
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::pushEngine.isInitialized) {
            "WalletClient needs to be initialized first using the initialize function"
        }
    }
}