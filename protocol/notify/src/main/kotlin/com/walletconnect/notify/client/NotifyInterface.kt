package com.walletconnect.notify.client

interface NotifyInterface {
    interface Delegate {

        fun onNotifySubscription(notifySubscribe: Notify.Event.Subscription)

        @Deprecated("We renamed this function to onNotifyMessage for consistency")
        fun onNotifyMessage(notifyMessage: Notify.Event.Message)

        fun onNotifyNotification(notifyNotification: Notify.Event.Notification)

        fun onNotifyDelete(notifyDelete: Notify.Event.Delete)

        fun onNotifyUpdate(notifyUpdate: Notify.Event.Update)

        fun onError(error: Notify.Model.Error)

        fun onSubscriptionsChanged(subscriptionsChanged: Notify.Event.SubscriptionsChanged)
    }

    fun initialize(init: Notify.Params.Init, onError: (Notify.Model.Error) -> Unit)

    fun setDelegate(delegate: Delegate)

    fun subscribe(params: Notify.Params.Subscribe, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun update(params: Notify.Params.Update, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getNotificationTypes(params: Notify.Params.NotificationTypes): Map<String, Notify.Model.NotificationType>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getActiveSubscriptions(): Map<String, Notify.Model.Subscription>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    @Deprecated("We renamed this function to getNotificationHistory for consistency")
    fun getMessageHistory(params: Notify.Params.MessageHistory): Map<Long, Notify.Model.MessageRecord>


    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getNotificationHistory(params: Notify.Params.NotificationHistory): Map<Long, Notify.Model.NotificationRecord>

    fun deleteSubscription(params: Notify.Params.DeleteSubscription, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit)

    @Deprecated("We renamed this function to deleteNotification for consistency")
    fun deleteNotifyMessage(params: Notify.Params.DeleteMessage, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun deleteNotification(params: Notify.Params.DeleteNotification, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit)

    @Deprecated("We renamed this function to decryptNotification for consistency")
    fun decryptMessage(params: Notify.Params.DecryptMessage, onSuccess: (Notify.Model.Message.Decrypted) -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun decryptNotification(params: Notify.Params.DecryptNotification, onSuccess: (Notify.Model.Notification.Decrypted) -> Unit, onError: (Notify.Model.Error) -> Unit)

    @Deprecated("We changed the registration flow to be more secure. Please use prepareRegistration and register instead")
    fun register(params: Notify.Params.Registration, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun register(params: Notify.Params.Register, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun prepareRegistration(params: Notify.Params.PrepareRegistration, onSuccess: (Notify.Model.CacaoPayloadWithIdentityPrivateKey, String) -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun isRegistered(params: Notify.Params.IsRegistered, onSuccess: (Boolean) -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun unregister(params: Notify.Params.Unregistration, onSuccess: (String) -> Unit, onError: (Notify.Model.Error) -> Unit)
}