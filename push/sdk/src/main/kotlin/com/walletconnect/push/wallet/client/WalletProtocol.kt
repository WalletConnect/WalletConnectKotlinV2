package com.walletconnect.push.wallet.client

import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.wallet.client.mapper.toClientPushRequest
import com.walletconnect.push.wallet.engine.domain.DecryptMessageUseCase
import com.walletconnect.push.wallet.engine.domain.WalletEngine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WalletProtocol : WalletInterface {
    private lateinit var decryptMessageUseCase: DecryptMessageUseCase
    private lateinit var pushEngine: WalletEngine

    companion object {
        val instance = WalletProtocol()
    }

    override fun initialize() {
        decryptMessageUseCase = wcKoinApp.koin.get()

        pushEngine = wcKoinApp.koin.get()
        pushEngine.setup()
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
            pushEngine.approve(params.id, onSuccess, onError)
        } catch (e: Exception) {
            onError(Push.Wallet.Model.Error(e))
        }
    }

    override fun reject(params: Push.Wallet.Params.Reject, onSuccess: (Boolean) -> Unit, onError: (Push.Wallet.Model.Error) -> Unit) {
        checkEngineInitialization()

        try {
            pushEngine.reject(params.id, params.reason, onSuccess, onError)
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
        TODO("Not yet implemented")
    }

    override fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (Push.Wallet.Model.Message) -> Unit, onError: (Push.Wallet.Model.Error) -> Unit) {
        runCatching { decryptMessageUseCase(params.topic, params.encryptedMessage) }.Enfold({ decryptedMsg ->
            onSuccess(Push.Wallet.Model.Message())
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