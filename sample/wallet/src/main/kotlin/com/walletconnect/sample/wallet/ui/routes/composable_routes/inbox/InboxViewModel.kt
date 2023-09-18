package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

class InboxViewModel : ViewModel() {
    val state = NotifyDelegate.notifyEvents
        .onEach { Timber.d("InboxViewModel event - $it") }
        .filter { event ->
            when (event) {
                is Notify.Event.Message, is Notify.Event.SubscriptionsChanged -> true
                else -> false
            }
        }
        .map { event ->
            when (event) {
                is Notify.Event.Message -> generateState(NotifyClient.getActiveSubscriptions().values.toList())
                is Notify.Event.SubscriptionsChanged -> generateState(event.subscriptions)
                else -> throw Throwable("It is simply not possible to hit this exception. I blame bit flip.")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), generateState(NotifyClient.getActiveSubscriptions().values.toList()))

    private fun generateState(subscriptions: List<Notify.Model.Subscription>): InboxState {
        val listOfActiveSubscriptions = subscriptions.map { subscription ->
            InboxState.Subscriptions.ActiveSubscriptions(
                topic = subscription.topic,
                icon = subscription.metadata.icons.first(),
                name = subscription.metadata.name,
                messageCount = NotifyClient.getMessageHistory(params = Notify.Params.MessageHistory(subscription.topic)).size
            )
        }

        return if (listOfActiveSubscriptions.isEmpty()) {
            InboxState.Empty
        } else {
            InboxState.Subscriptions(listOfActiveSubscriptions)
        }
    }
}

sealed interface InboxState {

    object Empty : InboxState

    data class Subscriptions(
        val listOfActiveSubscriptions: List<ActiveSubscriptions>,
    ) : InboxState {

        data class ActiveSubscriptions(
            val topic: String,
            val icon: String,
            val name: String,
            val messageCount: Int,
        )
    }
}