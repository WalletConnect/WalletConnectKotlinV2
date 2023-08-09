package com.walletconnect.notify.client

interface NotifyInterface {
    interface Delegate {

        fun onNotifySubscription(notifySubscribe: Notify.Event.Subscription)

        fun onNotifyMessage(notifyMessage: Notify.Event.Message)

        fun onNotifyDelete(notifyDelete: Notify.Event.Delete)

        fun onNotifyUpdate(notifyUpdate: Notify.Event.Update)

        fun onError(error: Notify.Model.Error)
    }

    fun initialize(init: Notify.Params.Init, onError: (Notify.Model.Error) -> Unit)

    fun setDelegate(delegate: Delegate)

    fun subscribe(params: Notify.Params.Subscribe, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun update(params: Notify.Params.Update, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getNotificationTypes(params: Notify.Params.NotificationTypes, onSuccess: (Notify.Model.AvailableTypes) -> Unit, onError: (Notify.Model.Error) -> Unit)

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getActiveSubscriptions(): Map<String, Notify.Model.Subscription>

    /**
     * Caution: This function is blocking and runs on the current thread.
     * It is advised that this function be called from background operation
     */
    fun getMessageHistory(params: Notify.Params.MessageHistory): Map<Long, Notify.Model.MessageRecord>

    fun deleteSubscription(params: Notify.Params.DeleteSubscription, onError: (Notify.Model.Error) -> Unit)

    fun deleteNotifyMessage(params: Notify.Params.DeleteMessage, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun decryptMessage(params: Notify.Params.DecryptMessage, onSuccess: (Notify.Model.Message) -> Unit, onError: (Notify.Model.Error) -> Unit)

    fun enableSync(params: Notify.Params.EnableSync, onSuccess: () -> Unit, onError: (Notify.Model.Error) -> Unit)
}