package com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.update_subscription

import androidx.lifecycle.ViewModel
import com.walletconnect.notify.client.NotifyClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UpdateSubscriptionViewModel : ViewModel() {
    private val currentSubscriptionTopic = MutableStateFlow("")

    fun setSubscriptionTopic(topic: String) {
        currentSubscriptionTopic.update { topic }
        _notificationTypes.update { NotifyClient.getActiveSubscriptions()[topic]!!.scope.map { (name, setting) -> name.value to Pair(setting.description, setting.enabled) }.toMap() }
    }

    val _notificationTypes = MutableStateFlow<Map<String, Pair<String, Boolean>>>(emptyMap())
    val notificationTypes = _notificationTypes.asStateFlow()

    fun updateNotificationType(name: String, value: Pair<String, Boolean>) {
        val types = _notificationTypes.value.toMutableMap()
        types[name] = value
        _notificationTypes.update { types }
    }
}