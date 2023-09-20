package com.walletconnect.push.client

import com.walletconnect.android.internal.common.di.DatabaseConfig
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.push.common.di.commonModule
import com.walletconnect.push.common.di.pushEngineUseCaseModules
import com.walletconnect.push.common.di.pushJsonRpcModule
import com.walletconnect.push.common.di.pushStorageModule
import com.walletconnect.push.common.model.EngineDO
import com.walletconnect.push.common.model.toClient
import com.walletconnect.push.common.model.toEvent
import com.walletconnect.push.common.model.toModel
import com.walletconnect.push.common.model.toWalletClient
import com.walletconnect.push.di.messageModule
import com.walletconnect.push.di.walletEngineModule
import com.walletconnect.push.engine.PushWalletEngine
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

    override fun initialize(init: Push.Params.Init, onError: (Push.Model.Error) -> Unit) {
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
            runBlocking(scope.coroutineContext) { pushWalletEngine.setup() }
        } catch (e: Exception) {
            onError(Push.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: PushWalletInterface.Delegate) {
        checkEngineInitialization()

        pushWalletEngine.engineEvent.onEach { event ->
            when (event) {
                is EngineDO.PushProposal -> delegate.onPushProposal(event.toWalletClient())
                is EngineDO.PushRecord -> delegate.onPushMessage(Push.Event.Message(event.toWalletClient()))
                is EngineDO.PushDelete -> delegate.onPushDelete(event.toWalletClient())
                is EngineDO.Subscription.Active -> delegate.onPushSubscription(event.toEvent())
                is EngineDO.Subscription.Error -> delegate.onPushSubscription(event.toWalletClient())
                is EngineDO.PushUpdate.Result -> delegate.onPushUpdate(event.toWalletClient())
                is EngineDO.PushUpdate.Error -> delegate.onPushUpdate(event.toWalletClient())
                is SDKError -> delegate.onError(event.toClient())
            }
        }.launchIn(scope)
    }

    override fun subscribe(params: Push.Params.Subscribe, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    pushWalletEngine.subscribeToDapp(params.dappUrl, params.account, params.onSign.toWalletClient(),
                        onSuccess = { _, _ -> onSuccess() },
                        onFailure = { onError(Push.Model.Error(it)) }
                    )
                } catch (e: Exception) {
                    onError(Push.Model.Error(e))
                }
            }
        }
    }

    override fun approve(params: Push.Params.Approve, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
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

    override fun reject(params: Push.Params.Reject, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
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

    override fun update(params: Push.Params.Update, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
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
            pushWalletEngine.getListOfActiveSubscriptions().mapValues { (_, subscriptionWMetadata) ->
                subscriptionWMetadata.toModel()
            }
        }
    }

    override fun getMessageHistory(params: Push.Params.MessageHistory): Map<Long, Push.Model.MessageRecord> {
        checkEngineInitialization()

        return runBlocking {
            pushWalletEngine.getListOfMessages(params.topic)
                .mapValues { (_, messageRecord) -> messageRecord.toWalletClient() }
        }
    }

    override fun deleteSubscription(params: Push.Params.DeleteSubscription, onError: (Push.Model.Error) -> Unit) {
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

    override fun deletePushMessage(params: Push.Params.DeleteMessage, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
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

    override fun decryptMessage(params: Push.Params.DecryptMessage, onSuccess: (Push.Model.Message) -> Unit, onError: (Push.Model.Error) -> Unit) {
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

    override fun enableSync(params: Push.Params.EnableSync, onSuccess: () -> Unit, onError: (Push.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                pushWalletEngine.enableSync(params.account, params.onSign.toWalletClient(), onSuccess, onFailure = { error -> onError(Push.Model.Error(error)) })
            }
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::pushWalletEngine.isInitialized) {
            "WalletClient needs to be initialized first using the initialize function"
        }
    }
}