package com.walletconnect.sample.wallet.ui.routes.bottomsheet_routes.update_subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.toUI
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class UpdateSubscriptionViewModelFactory(private val topic: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UpdateSubscriptionViewModel(topic) as T
    }
}

class UpdateSubscriptionViewModel(val topic: String) : ViewModel() {
    private val _activeSubscriptions = NotifyDelegate.notifyEvents
        .filter { event ->
            when (event) {
                is Notify.Event.SubscriptionsChanged -> true
                else -> false
            }
        }
        .debounce(500L)
        .map { event ->
            when (event) {
                is Notify.Event.SubscriptionsChanged -> event.subscriptions
                else -> throw Throwable("It is simply not possible to hit this exception. I blame bit flip.")
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NotifyClient.getActiveSubscriptions().values.toList())

    private val currentSubscription: Notify.Model.Subscription =
        _activeSubscriptions.value.firstOrNull { it.topic == topic } ?: throw IllegalStateException("No subscription found for topic $topic")

    val activeSubscriptionUI: MutableStateFlow<ActiveSubscriptionsUI> = MutableStateFlow(currentSubscription.toUI())


    private val _initialNotificationTypes = currentSubscription.scope.map { (id, setting) -> id.value to Triple(setting.name, setting.description, setting.enabled) }.toMap()
    private val _notificationTypes = MutableStateFlow(_initialNotificationTypes)
    val notificationTypes = _notificationTypes.asStateFlow()

    private val _state = MutableStateFlow<UpdateSubscriptionState>(UpdateSubscriptionState.Displaying)
    val state = _state.asStateFlow()

    val isUpdateEnabled = _notificationTypes.map { currentNotificationTypes ->
        (currentNotificationTypes != _initialNotificationTypes)
    }.combine(_state) { isUpdateEnabled, state ->
        isUpdateEnabled && state is UpdateSubscriptionState.Displaying
    }

    fun updateNotificationType(id: String, value: Triple<String, String, Boolean>) {
        val types = _notificationTypes.value.toMutableMap()
        types[id] = value
        _notificationTypes.update { types }
    }

    fun updateSubscription(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        val beforeSubscription = _activeSubscriptions.value
        _state.value = UpdateSubscriptionState.Updating

        NotifyClient.update(
            Notify.Params.Update(topic, _notificationTypes.value.filter { (_, value) -> value.third }.map { (name, _) -> name }),
            onSuccess = {
                viewModelScope.launch {
                    _activeSubscriptions.collect { afterSubscription ->
                        if (beforeSubscription != afterSubscription) {
                            onSuccess()
                            _state.value = UpdateSubscriptionState.Displaying
                            this.cancel()
                        }
                    }
                }
            },
            onError = { error ->
                onFailure(error.throwable)
            }
        )
    }
}


sealed interface UpdateSubscriptionState {
    object Updating : UpdateSubscriptionState
    object Displaying : UpdateSubscriptionState
}