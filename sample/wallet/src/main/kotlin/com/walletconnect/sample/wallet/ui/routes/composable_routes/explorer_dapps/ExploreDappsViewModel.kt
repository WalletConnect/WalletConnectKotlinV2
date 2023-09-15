package com.walletconnect.sample.wallet.ui.routes.composable_routes.explorer_dapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ExploreDappsViewModel : ViewModel() {
    val state = NotifyDelegate.notifyEvents
        .filterIsInstance<Notify.Event.SubscriptionsChanged>()
        .map {
            val listOfSubscriptions = NotifyClient.getActiveSubscriptions().toNotifySubscribedDappsList()

            ExplorerDappsState(explorerDapps = listOfSubscriptions)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ExplorerDappsState(NotifyClient.getActiveSubscriptions().toNotifySubscribedDappsList()))

    private fun Map<String, Notify.Model.Subscription>.toNotifySubscribedDappsList(): List<ExplorerDapp> = map { (topic, subscription) ->
        ExplorerDapp(
            topic = topic,
            icon = subscription.metadata.icons.first(),
            name = subscription.metadata.name,
            desc = subscription.metadata.description,
            url = subscription.metadata.url
        )
    }
}

data class ExplorerDappsState(val explorerDapps: List<ExplorerDapp>)