package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InboxViewModel : ViewModel() {
    val state = NotifyDelegate.notifyEvents
        .filterIsInstance<Notify.Event.SubscriptionsChanged>()
        .map {
            generateState()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), generateState())

    private fun generateState(): InboxState {
        val listOfActiveSubscriptions = NotifyClient.getActiveSubscriptions().map { (topic, subscription) ->
            InboxState.Subscriptions.ActiveSubscriptions(
                topic = topic,
                icon = subscription.metadata.icons.first(),
                name = subscription.metadata.name,
                messageCount = NotifyClient.getMessageHistory(params = Notify.Params.MessageHistory(topic)).size
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