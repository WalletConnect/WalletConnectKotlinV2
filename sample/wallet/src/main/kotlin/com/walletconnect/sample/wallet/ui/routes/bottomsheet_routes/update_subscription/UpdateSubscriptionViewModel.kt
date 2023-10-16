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
        _notificationTypes.update { NotifyClient.getActiveSubscriptions()[topic]!!.scope.map { (id, setting) -> id.value to Triple(setting.name, setting.description, setting.enabled) }.toMap() }
    }

    val _notificationTypes = MutableStateFlow<Map<String, Triple<String, String, Boolean>>>(emptyMap())
    val notificationTypes = _notificationTypes.asStateFlow()

    fun updateNotificationType(id: String, value: Triple<String, String, Boolean>) {
        val types = _notificationTypes.value.toMutableMap()
        types[id] = value
        _notificationTypes.update { types }
    }
}