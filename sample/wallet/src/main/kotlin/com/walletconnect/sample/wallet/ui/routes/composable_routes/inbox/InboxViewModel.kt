package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.explorer.data.model.Project
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover.ExplorerApp
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover.ImageUrl
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.toUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import com.walletconnect.android.internal.common.explorer.data.model.ImageUrl as WCImageUrl

class InboxViewModel(application: Application) : AndroidViewModel(application) {

    private val _subscriptionsState = MutableStateFlow<SubscriptionsState>(SubscriptionsState.Searching)
    val subscriptionsState = _subscriptionsState.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _activeSubscriptions = NotifyDelegate.notifyEvents
        .onEach { Timber.d("InboxViewModel event - $it") }
        .filter { event ->
            when (event) {
                is Notify.Event.Message, is Notify.Event.SubscriptionsChanged -> true
                else -> false
            }
        }
        .map { event ->
            when (event) {
                is Notify.Event.Message -> getActiveSubscriptions(NotifyClient.getActiveSubscriptions().values.toList())
                is Notify.Event.SubscriptionsChanged -> getActiveSubscriptions(event.subscriptions)
                else -> throw Throwable("It is simply not possible to hit this exception. I blame bit flip.")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), getActiveSubscriptions(NotifyClient.getActiveSubscriptions().values.toList()))

    val activeSubscriptions = searchText
        .debounce(500L)
        .filter { _subscriptionsState.value !is SubscriptionsState.Failure }
        .onEach { _subscriptionsState.update { SubscriptionsState.Searching } }
        .combine(_activeSubscriptions) { text, activeSubscriptions ->
            if (text.isBlank()) {
                activeSubscriptions
            } else {
                delay(500L)
                activeSubscriptions.filter { it.doesMatchSearchQuery(text) }
            }.also {
                if (activeSubscriptions.isEmpty()) _subscriptionsState.update { SubscriptionsState.Unsubscribed }
                else _subscriptionsState.update { SubscriptionsState.Success }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _activeSubscriptions.value)

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    private fun getActiveSubscriptions(subscriptions: List<Notify.Model.Subscription>): List<ActiveSubscriptionsUI> {
        return subscriptions.map { subscription -> subscription.toUI() }
    }


    private val _discoverState = MutableStateFlow<DiscoverState>(DiscoverState.Searching)
    val discoverState = _discoverState.asStateFlow()

    private suspend fun getExplorerProjects() = CoreClient.Explorer.getProjects(0, 500, false)

    private val _explorerApps = MutableStateFlow(emptyList<ExplorerApp>())
    val explorerApps = searchText
        .debounce(500L)
        .filter { _discoverState.value !is DiscoverState.Failure }
        .onEach { _discoverState.update { DiscoverState.Searching } }
        .combine(_explorerApps) { text, projects ->
            if (text.isBlank()) {
                projects
            } else {
                delay(500L)
                projects.filter { it.doesMatchSearchQuery(text) }
            }
        }
        .combine(_activeSubscriptions) { projects, activeSubscriptions ->
            projects.map { explorerApp ->
                activeSubscriptions.find { it.appDomain == explorerApp.dappUrl }?.let {
                    explorerApp.copy(isSubscribed = true, topic = it.topic)
                } ?: explorerApp
            }
        }
        .onEach { _discoverState.update { DiscoverState.Success } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _explorerApps.value)

    init {
        viewModelScope.launch {
            fetchExplorerApps()
        }
    }

    private suspend fun fetchExplorerApps() {
        _explorerApps.value = getExplorerProjects().fold(onFailure = { error -> _discoverState.update { DiscoverState.Failure(error) }; emptyList() }, onSuccess = { it.toExplorerApp() })
    }

    private fun List<Project>.toExplorerApp(): List<ExplorerApp> = map { project ->
        with(project) {
            ExplorerApp(id, name, homepage, imageId, description, imageUrl.toExplorerApp(), dappUrl, false)
        }
    }

    private fun WCImageUrl.toExplorerApp(): ImageUrl =
        ImageUrl(sm, md, lg)
}

sealed interface SubscriptionsState {

    object Searching : SubscriptionsState
    data class Failure(val error: Throwable) : SubscriptionsState
    object Success : SubscriptionsState
    object Unsubscribed : SubscriptionsState

}


sealed interface DiscoverState {
    object Searching : DiscoverState
    data class Failure(val error: Throwable) : DiscoverState
    object Success : DiscoverState
}
