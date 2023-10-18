package com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.update_subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.toUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Suppress("UNCHECKED_CAST")
class UpdateSubscriptionViewModelFactory(private val topic: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UpdateSubscriptionViewModel(topic) as T
    }
}

class UpdateSubscriptionViewModel(topic: String) : ViewModel() {
    private val currentSubscription: Notify.Model.Subscription = NotifyClient.getActiveSubscriptions()[topic] ?: throw IllegalStateException("No subscription found for topic $topic")
    val activeSubscriptionUI: MutableStateFlow<ActiveSubscriptionsUI> = MutableStateFlow(currentSubscription.toUI())


    val _notificationTypes = MutableStateFlow(currentSubscription.scope.map { (id, setting) -> id.value to Triple(setting.name, setting.description, setting.enabled) }.toMap() )
    val notificationTypes = _notificationTypes.asStateFlow()

    fun updateNotificationType(id: String, value: Triple<String, String, Boolean>) {
        val types = _notificationTypes.value.toMutableMap()
        types[id] = value
        _notificationTypes.update { types }
    }
}