package com.walletconnect.notify.client

import com.walletconnect.android.Core
import com.walletconnect.android.internal.common.di.DatabaseConfig
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.scope
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.notify.common.model.Notification
import com.walletconnect.notify.common.model.SubscriptionChanged
import com.walletconnect.notify.common.model.toClient
import com.walletconnect.notify.common.model.toCommon
import com.walletconnect.notify.di.engineModule
import com.walletconnect.notify.di.notifyJsonRpcModule
import com.walletconnect.notify.di.notifyStorageModule
import com.walletconnect.notify.engine.NotifyEngine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinApplication

class NotifyProtocol(private val koinApp: KoinApplication = wcKoinApp) : NotifyInterface {
    private lateinit var notifyEngine: NotifyEngine

    companion object {
        val instance = NotifyProtocol()
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
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

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun setDelegate(delegate: NotifyInterface.Delegate) {
        checkEngineInitialization()

        notifyEngine.engineEvent.onEach { event ->
            when (event) {
                is Notification -> delegate.onNotifyNotification(Notify.Event.Notification(event.toClient()))
                is SubscriptionChanged -> delegate.onSubscriptionsChanged(event.toClient())
                is SDKError -> delegate.onError(event.toClient())
            }
        }.launchIn(scope)
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun subscribe(params: Notify.Params.Subscribe): Notify.Result.Subscribe {
        checkEngineInitialization()

        return runBlocking {
            try {
                notifyEngine.subscribeToDapp(params.appDomain, params.account, params.timeout).toClient()
            } catch (e: Exception) {
                Notify.Result.Subscribe.Error(Notify.Model.Error(e))
            }
        }
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun updateSubscription(params: Notify.Params.UpdateSubscription): Notify.Result.UpdateSubscription {
        checkEngineInitialization()

        return runBlocking {
            try {
                notifyEngine.update(params.topic, params.scope, params.timeout).toClient()
            } catch (e: Exception) {
                Notify.Result.UpdateSubscription.Error(Notify.Model.Error(e))
            }
        }
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun getNotificationTypes(params: Notify.Params.GetNotificationTypes): Map<String, Notify.Model.NotificationType> {
        checkEngineInitialization()

        return runBlocking {
            notifyEngine.getNotificationTypes(params.appDomain, params.timeout).mapValues { (_, notificationType) -> notificationType.toClient() }
        }
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun getActiveSubscriptions(params: Notify.Params.GetActiveSubscriptions): Map<String, Notify.Model.Subscription> {
        checkEngineInitialization()

        return runBlocking {
            notifyEngine.getActiveSubscriptions(params.account, params.timeout).mapValues { (_, subscriptionWMetadata) -> subscriptionWMetadata.toClient() }
        }
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun getNotificationHistory(params: Notify.Params.GetNotificationHistory): Notify.Result.GetNotificationHistory {
        checkEngineInitialization()

        return runBlocking {
            try {
                notifyEngine.getNotificationHistory(params.topic, params.limit, params.startingAfter, params.timeout).toClient()
            } catch (e: Exception) {
                Notify.Result.GetNotificationHistory.Error(Notify.Model.Error(e))
            }
        }
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun deleteSubscription(params: Notify.Params.DeleteSubscription): Notify.Result.DeleteSubscription {
        checkEngineInitialization()

        return runBlocking {
            try {
                notifyEngine.deleteSubscription(params.topic, params.timeout).toClient()
            } catch (e: Exception) {
                Notify.Result.DeleteSubscription.Error(Notify.Model.Error(e))
            }
        }
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
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

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun register(params: Notify.Params.Register, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit) {
        checkEngineInitialization()

        scope.launch {
            notifyEngine.register(

                cacaoPayloadWithIdentityPrivateKey = params.cacaoPayloadWithIdentityPrivateKey.toCommon(),
                signature = params.signature.toCommon(),
                onSuccess = { onSuccess(it) },
                onFailure = { error -> onError(Notify.Model.Error(error)) },
            )
        }
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun isRegistered(params: Notify.Params.IsRegistered): Boolean {
        checkEngineInitialization()

        return runBlocking {
            notifyEngine.isRegistered(account = params.account, domain = params.domain)
        }
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun prepareRegistration(
        params: Notify.Params.PrepareRegistration,
        onSuccess: (Notify.Model.CacaoPayloadWithIdentityPrivateKey, String) -> Unit,
        onError: (Notify.Model.Error) -> Unit
    ) {
        checkEngineInitialization()

        scope.launch {
            notifyEngine.prepareRegistration(
                account = params.account,
                domain = params.domain,
                onSuccess = { cacaoPayloadWithIdentityPrivateKey, message -> onSuccess(cacaoPayloadWithIdentityPrivateKey.toClient(), message) },
                onFailure = { error -> onError(Notify.Model.Error(error)) },
            )
        }
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    override fun unregister(params: Notify.Params.Unregister, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit) {
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