package com.walletconnect.notify.client

@Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
interface NotifyInterface {
    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    interface Delegate {
        fun onNotifyNotification(notifyNotification: Notify.Event.Notification)

        fun onError(error: Notify.Model.Error)

        fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged)
    }

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun initialize(init: Notify.Params.Init, onError: (Notify.Model.Error) -> Unit)

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun setDelegate(delegate: Delegate)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun subscribe(params: Notify.Params.Subscribe) : Notify.Result.Subscribe

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun updateSubscription(params: Notify.Params.UpdateSubscription) : Notify.Result.UpdateSubscription

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun deleteSubscription(params: Notify.Params.DeleteSubscription): Notify.Result.DeleteSubscription

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getNotificationTypes(params: Notify.Params.GetNotificationTypes): Map<String, Notify.Model.NotificationType>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getActiveSubscriptions(params: Notify.Params.GetActiveSubscriptions): Map<String, Notify.Model.Subscription>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun getNotificationHistory(params: Notify.Params.GetNotificationHistory): Notify.Result.GetNotificationHistory

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun decryptNotification(params: Notify.Params.DecryptNotification, onSuccess: (Notify.Model.Notification.Decrypted) -> Unit, onError: (Notify.Model.Error) -> Unit)

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun register(params: Notify.Params.Register, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit)

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun prepareRegistration(params: Notify.Params.PrepareRegistration, onSuccess: (Notify.Model.CacaoPayloadWithIdentityPrivateKey, String) -> Unit, onError: (Notify.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun isRegistered(params: Notify.Params.IsRegistered): Boolean

    @Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
    fun unregister(params: Notify.Params.Unregister, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit)
}