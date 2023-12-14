package com.walletconnect.notify.client

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.di.DatabaseConfig
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.notify.common.model.DeleteSubscription
import com.walletconnect.notify.common.model.Error
import com.walletconnect.notify.common.model.NotifyRecord
import com.walletconnect.notify.common.model.Subscription
import com.walletconnect.notify.common.model.SubscriptionChanged
import com.walletconnect.notify.common.model.UpdateSubscription
import com.walletconnect.notify.common.model.toClient
import com.walletconnect.notify.common.model.toCommon
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
                is NotifyRecord -> {
                    delegate.onNotifyMessage(Notify.Event.Message(event.toWalletClient()))
                    delegate.onNotifyNotification(Notify.Event.Notification(event.toClient()))
                }

                is UpdateSubscription.Result -> delegate.onNotifyUpdate(event.toWalletClient())
                is UpdateSubscription.Error -> delegate.onNotifyUpdate(event.toWalletClient())
                is DeleteSubscription -> delegate.onNotifyDelete(event.toWalletClient())
                is SDKError -> delegate.onError(event.toClient())
                is SubscriptionChanged -> delegate.onSubscriptionsChanged(event.toWalletClient())
            }
        }.launchIn(scope)
    }

    override fun subscribe(params: Notify.Params.Subscribe, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    notifyEngine.subscribeToDapp(params.appDomain, params.account,
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

    override fun getNotificationTypes(params: Notify.Params.NotificationTypes): Map<String, Notify.Model.NotificationType> {
        checkEngineInitialization()

        return runBlocking {
            notifyEngine.getNotificationTypes(params.appDomain).mapValues { (_, notificationType) ->
                notificationType.toWalletClient()
            }
        }
    }


    override fun getActiveSubscriptions(): Map<String, Notify.Model.Subscription> {
        checkEngineInitialization()

        return runBlocking {
            notifyEngine.getListOfActiveSubscriptions().mapValues { (_, subscriptionWMetadata) ->
                subscriptionWMetadata.toModel()
            }
        }
    }

    @Deprecated("We renamed this function to getNotificationHistory for consistency")
    override fun getMessageHistory(params: Notify.Params.MessageHistory): Map<Long, Notify.Model.MessageRecord> {
        checkEngineInitialization()

        return runBlocking {
            notifyEngine.getListOfNotifications(params.topic).mapValues { (_, messageRecord) -> messageRecord.toWalletClient() }
        }
    }

    override fun getNotificationHistory(params: Notify.Params.NotificationHistory): Map<Long, Notify.Model.NotificationRecord> {
        checkEngineInitialization()

        return runBlocking { notifyEngine.getListOfNotifications(params.topic).mapValues { (_, notifyRecord) -> notifyRecord.toClient() } }
    }

    override fun deleteSubscription(params: Notify.Params.DeleteSubscription, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    notifyEngine.deleteSubscription(params.topic, onSuccess) { error -> onError(Notify.Model.Error(error)) }
                } catch (e: Exception) {
                    onError(Notify.Model.Error(e))
                }
            }
        }
    }

    @Deprecated("We renamed this function to deleteNotification for consistency")
    override fun deleteNotifyMessage(params: Notify.Params.DeleteMessage, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    notifyEngine.deleteNotification(params.id, onSuccess) { error -> onError(Notify.Model.Error(error)) }
                } catch (e: Exception) {
                    onError(Notify.Model.Error(e))
                }
            }
        }
    }

    override fun deleteNotification(params: Notify.Params.DeleteNotification, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            supervisorScope {
                try {
                    notifyEngine.deleteNotification(params.id, onSuccess) { error -> onError(Notify.Model.Error(error)) }
                } catch (e: Exception) {
                    onError(Notify.Model.Error(e))
                }
            }
        }
    }

    @Deprecated("We renamed this function to decryptNotification for consistency")
    override fun decryptMessage(params: Notify.Params.DecryptMessage, onSuccess: (Notify.Model.Message.Decrypted) -> Unit, onError: (Notify.Model.Error) -> Unit) {
        scope.launch {
            notifyEngine.decryptNotification(params.topic, params.encryptedMessage,
                onSuccess = { notifyMessage ->
                    (notifyMessage as? Core.Model.Message.Notify)?.run { onSuccess(notifyMessage.toWalletClient(params.topic)) }
                },
                onFailure = { error -> onError(Notify.Model.Error(error)) }
            )
        }
    }

    override fun decryptNotification(params: Notify.Params.DecryptNotification, onSuccess: (Notify.Model.Notification.Decrypted) -> Unit, onError: (Notify.Model.Error) -> Unit) {
        scope.launch {
            notifyEngine.decryptNotification(params.topic, params.encryptedMessage,
                onSuccess = { notification ->
                    (notification as? Core.Model.Message.Notify)?.run { onSuccess(notification.toClient(params.topic)) }
                },
                onFailure = { error -> onError(Notify.Model.Error(error)) }
            )
        }
    }

    @Deprecated("We changed the registration flow to be more secure. Please use prepareRegistration and register instead")
    override fun register(params: Notify.Params.Registration, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            notifyEngine.legacyRegister(
                params.account,
                params.isLimited,
                params.domain,
                params.onSign.toWalletClient(),
                onSuccess = onSuccess,
                onFailure = { error -> onError(Notify.Model.Error(error)) }
            )
        }
    }

    override fun register(params: Notify.Params.Register, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            notifyEngine.register(
                cacaoPayloadWithIdentityPrivateKey = params.cacaoPayloadWithIdentityPrivateKey.toCommon(),
                signature = params.signature.toCommon(),
                onSuccess = onSuccess,
                onFailure = { error -> onError(Notify.Model.Error(error)) },
            )
        }
    }

    override fun isRegistered(params: Notify.Params.IsRegistered): Boolean {
        checkEngineInitialization()

        return runBlocking {
            notifyEngine.isRegistered(account = params.account, domain = params.domain, allApps = params.allApps)
        }
    }

    override fun prepareRegistration(params: Notify.Params.PrepareRegistration, onSuccess: (Notify.Model.CacaoPayloadWithIdentityPrivateKey, String) -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            notifyEngine.prepareRegistration(
                account = params.account,
                domain = params.domain,
                allApps = params.allApps,
                onSuccess = { cacaoPayloadWithIdentityPrivateKey, message -> onSuccess(cacaoPayloadWithIdentityPrivateKey.toClient(), message) },
                onFailure = { error -> onError(Notify.Model.Error(error)) },
            )
        }
    }

    override fun unregister(params: Notify.Params.Unregistration, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            notifyEngine.unregister(
                params.account,
                onSuccess = onSuccess,
                onFailure = { error -> onError(Notify.Model.Error(error)) }
            )
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkEngineInitialization() {
        check(::notifyEngine.isInitialized) { "WalletClient needs to be initialized first using the initialize function" }
    }
}