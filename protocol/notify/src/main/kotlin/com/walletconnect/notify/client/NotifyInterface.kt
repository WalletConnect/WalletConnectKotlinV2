package com.walletconnect.notify.client

interface NotifyInterface {
    interface Delegate {
        //todo: migration guide and remove all deprecations
        fun onNotifyNotification(notifyNotification: Notify.Event.Notification)

        fun onError(error: Notify.Model.Error)

        fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged)
    }

    fun initialize(init: Notify.Params.Init, onError: (Notify.Model.Error) -> Unit)

    fun setDelegate(delegate: Delegate)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun subscribe(params: Notify.Params.Subscribe) : Notify.Result.Subscribe

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun updateSubscription(params: Notify.Params.UpdateSubscription) : Notify.Result.UpdateSubscription

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun deleteSubscription(params: Notify.Params.DeleteSubscription): Notify.Result.DeleteSubscription

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getNotificationTypes(params: Notify.Params.GetNotificationTypes): Map<String, Notify.Model.NotificationType>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getActiveSubscriptions(): Map<String, Notify.Model.Subscription>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getNotificationHistory(params: Notify.Params.GetNotificationHistory): Map<Long, Notify.Model.NotificationRecord>


    fun decryptNotification(params: Notify.Params.DecryptNotification, onSuccess: (Notify.Model.Notification.Decrypted) -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun register(params: Notify.Params.Register, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun prepareRegistration(params: Notify.Params.PrepareRegistration, onSuccess: (Notify.Model.CacaoPayloadWithIdentityPrivateKey, String) -> Unit, onError: (Notify.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun isRegistered(params: Notify.Params.IsRegistered): Boolean

    fun unregister(params: Notify.Params.Unregister, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit)
}