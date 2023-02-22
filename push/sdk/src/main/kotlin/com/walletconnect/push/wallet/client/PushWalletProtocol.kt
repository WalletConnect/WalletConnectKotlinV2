package com.walletconnect.push.wallet.client

import com.walletconnect.android.internal.common.di.DBUtils
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.Push
import com.walletconnect.push.common.di.pushJsonRpcModule
import com.walletconnect.push.common.di.pushStorageModule
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.toClient
import com.walletconnect.push.wallet.client.mapper.toClient
import com.walletconnect.push.wallet.client.mapper.toClientEvent
import com.walletconnect.push.wallet.client.mapper.toClientModel
import com.walletconnect.push.common.di.pushStorageModule
import com.walletconnect.push.wallet.di.walletEngineModule
import com.walletconnect.push.wallet.engine.PushWalletEngine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PushWalletProtocol : PushWalletInterface {
    private lateinit var pushWalletEngine: PushWalletEngine

    companion object {
        val instance = PushWalletProtocol()
    }

    override fun initialize(init: Push.Wallet.Params.Init, onError: (Push.Model.Error) -> Unit) {
        try {
            wcKoinApp.modules(
                pushJsonRpcModule(),
                pushStorageModule(DBUtils.PUSH_WALLET_SDK_DB_NAME),
                walletEngineModule(),
            )

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
                is EngineDO.PushMessage -> delegate.onPushMessage(event.toClientEvent())
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

    override fun getMessageHistory(params: Push.Wallet.Params.MessageHistory): Map<String, Push.Model.Subscription> {
        checkEngineInitialization()

        return pushWalletEngine.getListOfActiveSubscriptions()
            .filterKeys { topic -> topic == params.topic }
            .mapValues { (_, subscription) -> subscription.toClient() }
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
        pushWalletEngine.decryptMessage(params.topic, params.encryptedMessage,
            onSuccess = { pushMessage ->
                onSuccess(pushMessage.toClientModel())
            },
            onError = { error ->
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