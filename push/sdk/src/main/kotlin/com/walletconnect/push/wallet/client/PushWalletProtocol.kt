package com.walletconnect.push.wallet.client

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
import com.walletconnect.push.common.model.toCommonClient
import com.walletconnect.push.common.model.toWalletClient
import com.walletconnect.push.wallet.di.messageModule
import com.walletconnect.push.wallet.di.walletEngineModule
import com.walletconnect.push.wallet.engine.PushWalletEngine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.koin.core.KoinApplication

class PushWalletProtocol(private val koinApp: KoinApplication = wcKoinApp) : PushWalletInterface {
    private lateinit var pushWalletEngine: PushWalletEngine

    companion object {
        val instance = PushWalletProtocol()
    }

    override fun initialize(init: Push.Wallet.Params.Init, onError: (Push.Model.Error) -> Unit) {
        try {
            koinApp.modules(
                pushJsonRpcModule(),
                pushStorageModule(koinApp.koin.get<DatabaseConfig>().PUSH_WALLET_SDK_DB_NAME),
                walletEngineModule(),
                messageModule(),
                commonModule(),
                pushEngineUseCaseModules()
            )

            pushWalletEngine = koinApp.koin.get()
            pushWalletEngine.setup()
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: PushWalletInterface.Delegate) {
        checkEngineInitialization()

        pushWalletEngine.engineEvent.onEach { event ->
            when (event) {
                is EngineDO.PushPropose -> delegate.onPushProposal(event.toWalletClient())
                is EngineDO.PushRecord -> delegate.onPushMessage(Push.Wallet.Event.Message(event.toWalletClient()))
                is EngineDO.PushDelete -> delegate.onPushDelete(event.toWalletClient())
                is EngineDO.PushSubscription -> delegate.onPushSubscription(event.toWalletClient())
                is EngineDO.PushSubscribeError -> delegate.onPushSubscription(event.toWalletClient())
                is EngineDO.PushUpdate -> delegate.onPushUpdate(event.toWalletClient())
                is EngineDO.PushUpdateError -> delegate.onPushUpdate(event.toWalletClient())
                is SDKError -> delegate.onError(event.toClient())
            }
        }.launchIn(scope)
    }

    override fun subscribe(params: Push.Wallet.Params.Subscribe, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    pushWalletEngine.subscribeToDapp(params.dappUrl, params.account, params.onSign.toWalletClient(),
                        onSuccess = { _, _ ->
                            onSuccess()
                        },
                        onFailure = {
                            onError(Push.Model.Error(it))
                        }
                    )
                } catch (e: Exception) {
                    onError(Push.Model.Error(e))
                }
            }
        }
    }

    override fun approve(params: Push.Wallet.Params.Approve, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    pushWalletEngine.approve(
                        params.id,
                        params.onSign.toWalletClient(),
                        onSuccess
                    ) {
                        onError(Push.Model.Error(it))
                    }
                } catch (e: Exception) {
                    onError(Push.Model.Error(e))
                }
            }
        }
    }

    override fun reject(params: Push.Wallet.Params.Reject, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    pushWalletEngine.reject(params.id, params.reason, onSuccess) { onError(Push.Model.Error(it)) }
                } catch (e: Exception) {
                    onError(Push.Model.Error(e))
                }
            }
        }
    }

    override fun update(params: Push.Wallet.Params.Update, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    pushWalletEngine.update(params.topic, params.scope, onSuccess) { onError(Push.Model.Error(it)) }
                } catch (e: Exception) {
                    onError(Push.Model.Error(e))
                }
            }
        }
    }

    override fun getActiveSubscriptions(): Map<String, Push.Model.Subscription> {
        checkEngineInitialization()

        return runBlocking {
            pushWalletEngine.getListOfActiveSubscriptions().mapValues { (_, subscription) ->
                subscription.toCommonClient()
            }
        }
    }

    override fun getMessageHistory(params: Push.Wallet.Params.MessageHistory): Map<Long, Push.Model.MessageRecord> {
        checkEngineInitialization()

        return runBlocking {
            pushWalletEngine.getListOfMessages(params.topic)
                .mapValues { (_, messageRecord) -> messageRecord.toWalletClient() }
        }
    }

    override fun deleteSubscription(params: Push.Wallet.Params.DeleteSubscription, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    pushWalletEngine.deleteSubscription(params.topic) { error -> onError(Push.Model.Error(error)) }
                } catch (e: Exception) {
                    onError(Push.Model.Error(e))
                }
            }
        }
    }

    override fun deletePushMessage(params: Push.Wallet.Params.DeleteMessage, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    pushWalletEngine.deleteMessage(params.id, onSuccess) { error -> onError(Push.Model.Error(error)) }
                } catch (e: Exception) {
                    onError(Push.Model.Error(e))
                }
            }
        }
    }

    override fun decryptMessage(params: Push.Wallet.Params.DecryptMessage, onSuccess: (Push.Model.Message) -> Unit, onError: (Push.Model.Error) -> Unit) {
        scope.launch {
            pushWalletEngine.decryptMessage(params.topic, params.encryptedMessage,
                onSuccess = { pushMessage ->
                    onSuccess(pushMessage.toWalletClient())
                },
                onFailure = { error ->
                    onError(Push.Model.Error(error))
                }
            )
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::pushWalletEngine.isInitialized) {
            "WalletClient needs to be initialized first using the initialize function"
        }
    }
}