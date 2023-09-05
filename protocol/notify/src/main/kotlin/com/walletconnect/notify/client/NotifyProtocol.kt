package com.walletconnect.notify.client

import com.walletconnect.android.internal.common.di.DatabaseConfig
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.notify.common.model.DeleteSubscription
import com.walletconnect.notify.common.model.Error
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.common.model.UpdateSubscription
import com.walletconnect.notify.common.model.toClient
import com.walletconnect.notify.common.model.toEvent
import com.walletconnect.notify.common.model.toModel
import com.walletconnect.notify.common.model.toWalletClient
import com.walletconnect.notify.di.engineModule
import com.walletconnect.notify.di.notifyJsonRpcModule
import com.walletconnect.notify.di.notifyStorageModule
import com.walletconnect.notify.engine.NotifyEngine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.koin.core.KoinApplication

class NotifyProtocol(private val koinApp: KoinApplication = wcKoinApp) : NotifyInterface {
    private lateinit var notifyEngine: NotifyEngine

    companion object {
        val instance = NotifyProtocol()
    }

    override fun initialize(init: Notify.Params.Init, onError: (Notify.Model.Error) -> Unit) {
        try {
            koinApp.modules(
                notifyJsonRpcModule(),
                notifyStorageModule(koinApp.koin.get<DatabaseConfig>().NOTIFY_SDK_DB_NAME),
                engineModule(),
            )

            notifyEngine = koinApp.koin.get()
            runBlocking(scope.coroutineContext) { notifyEngine.setup() }
        } catch (e: Exception) {
            onError(Notify.Model.Error(e))
        }
    }

    override fun setDelegate(delegate: NotifyInterface.Delegate) {
        checkEngineInitialization()

        notifyEngine.engineEvent.onEach { event ->
            when (event) {
                is Subscription.Active -> delegate.onNotifySubscription(event.toEvent())
                is Error -> delegate.onNotifySubscription(event.toWalletClient())
                is NotifyRecord -> delegate.onNotifyMessage(Notify.Event.Message(event.toWalletClient()))
                is UpdateSubscription.Result -> delegate.onNotifyUpdate(event.toWalletClient())
                is UpdateSubscription.Error -> delegate.onNotifyUpdate(event.toWalletClient())
                is DeleteSubscription -> delegate.onNotifyDelete(event.toWalletClient())
                is SDKError -> delegate.onError(event.toClient())
            }
        }.launchIn(scope)
    }

    override fun subscribe(params: Notify.Params.Subscribe, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    notifyEngine.subscribeToDapp(params.dappUrl, params.account,
                        onSuccess = { _, _ -> onSuccess() },
                        onFailure = { onError(Notify.Model.Error(it)) }
                    )
                } catch (e: Exception) {
                    onError(Notify.Model.Error(e))
                }
            }
        }
    }

    override fun update(params: Notify.Params.Update, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    notifyEngine.update(params.topic, params.scope, onSuccess) { onError(Notify.Model.Error(it)) }
                } catch (e: Exception) {
                    onError(Notify.Model.Error(e))
                }
            }
        }
    }

    // TODO: Will add back later
//    override fun getNotificationTypes(params: Notify.Params.NotificationTypes, onSuccess: (Notify.Model.AvailableTypes) -> Unit, onError: (Notify.Model.Error) -> Unit) {
//        checkEngineInitialization()
//
//        scope.launch {
//            supervisorScope {
//                try {
//                    notifyEngine.getNotificationTypes(params.domain, onSuccess = {
//                        // TODO onSuccess(Notify.Model.AvailableTypes(it))
//                    }, onError = { onError(Notify.Model.Error(it)) })
//                } catch (e: Exception) {
//                    onError(Notify.Model.Error(e))
//                }
//            }
//        }
//    }

    override fun getActiveSubscriptions(): Map<String, Notify.Model.Subscription> {
        checkEngineInitialization()

        return runBlocking {
            notifyEngine.getListOfActiveSubscriptions().mapValues { (_, subscriptionWMetadata) ->
                subscriptionWMetadata.toModel()
            }
        }
    }

    override fun getMessageHistory(params: Notify.Params.MessageHistory): Map<Long, Notify.Model.MessageRecord> {
        checkEngineInitialization()

        return runBlocking {
            notifyEngine.getListOfMessages(params.topic)
                .mapValues { (_, messageRecord) -> messageRecord.toWalletClient() }
        }
    }

    override fun deleteSubscription(params: Notify.Params.DeleteSubscription, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    notifyEngine.deleteSubscription(params.topic) { error -> onError(Notify.Model.Error(error)) }
                } catch (e: Exception) {
                    onError(Notify.Model.Error(e))
                }
            }
        }
    }

    override fun deleteNotifyMessage(params: Notify.Params.DeleteMessage, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    notifyEngine.deleteMessage(params.id, onSuccess) { error -> onError(Notify.Model.Error(error)) }
                } catch (e: Exception) {
                    onError(Notify.Model.Error(e))
                }
            }
        }
    }

    override fun decryptMessage(params: Notify.Params.DecryptMessage, onSuccess: (Notify.Model.Message) -> Unit, onError: (Notify.Model.Error) -> Unit) {
        scope.launch {
            notifyEngine.decryptMessage(params.topic, params.encryptedMessage,
                onSuccess = { notifyMessage ->
                    onSuccess(notifyMessage.toWalletClient())
                },
                onFailure = { error ->
                    onError(Notify.Model.Error(error))
                }
            )
        }
    }

    override fun register(params: Notify.Params.Registration, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            notifyEngine.register(
                params.account,
                params.onSign.toWalletClient(),
                onSuccess = onSuccess,
                onFailure = { error ->
                    onError(Notify.Model.Error(error))
                }
            )
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::notifyEngine.isInitialized) {
            "WalletClient needs to be initialized first using the initialize function"
        }
    }
}