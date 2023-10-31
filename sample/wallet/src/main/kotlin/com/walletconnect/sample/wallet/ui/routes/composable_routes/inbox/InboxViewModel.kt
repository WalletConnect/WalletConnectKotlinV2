@file:OptIn(FlowPreview::class)

package com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.explorer.data.model.Project
import com.walletconnect.notify.client.Notify
import com.walletconnect.notify.client.NotifyClient
import com.walletconnect.sample.wallet.domain.EthAccountDelegate
import com.walletconnect.sample.wallet.domain.NotifyDelegate
import com.walletconnect.sample.wallet.domain.toEthAddress
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.toUI
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover.ExplorerApp
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover.ImageUrl
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.walletconnect.android.internal.common.explorer.data.model.ImageUrl as WCImageUrl

class InboxViewModel(application: Application) : AndroidViewModel(application) {

    private val _subscriptionsState = MutableStateFlow<SubscriptionsState>(SubscriptionsState.Searching)
    val subscriptionsState = _subscriptionsState.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

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
                is Notify.Event.SubscriptionsChanged -> getActiveSubscriptions(event.subscriptions)
                else -> throw Throwable("It is simply not possible to hit this exception. I blame bit flip.")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), getActiveSubscriptions(NotifyClient.getActiveSubscriptions().values.toList()))

    private val _activeSubscriptionsTrigger = MutableSharedFlow<Unit>(replay = 1)

    val activeSubscriptions = _activeSubscriptionsTrigger
        .debounce(500L)
        .filter { _subscriptionsState.value !is SubscriptionsState.Failure }
        .onEach { _subscriptionsState.update { SubscriptionsState.Searching } }
        .combine(_activeSubscriptions) { _, activeSubscriptions ->
            val text = searchText.value
            if (text.isBlank()) {
                activeSubscriptions
            } else {
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


    private val _discoverState = MutableStateFlow<DiscoverState>(DiscoverState.Fetching)
    val discoverState = _discoverState.asStateFlow()

    private suspend fun getExplorerProjects() = CoreClient.Explorer.getProjects(0, 500, false)

    private val _explorerApps = MutableStateFlow(emptyList<ExplorerApp>())
    private val _explorerAppsTrigger = MutableSharedFlow<Unit>(replay = 1)
    val explorerApps = _explorerAppsTrigger
        .debounce(500L)
        .filter { _discoverState.value !is DiscoverState.Failure }
        .onEach { _discoverState.update { DiscoverState.Searching } }
        .combine(_explorerApps) { _, projects ->
            val text = searchText.value
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
        .onEach { _discoverState.update { DiscoverState.Fetched } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _explorerApps.value)

    init {
        searchText.onEach {
            _explorerAppsTrigger.emit(Unit)
            _activeSubscriptionsTrigger.emit(Unit)
        }.launchIn(viewModelScope)

        _activeSubscriptions.onEach {
            _activeSubscriptionsTrigger.emit(Unit)
        }.launchIn(viewModelScope)

        _explorerApps.onEach {
            _explorerAppsTrigger.emit(Unit)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            fetchExplorerApps()
        }
    }

    private suspend fun fetchExplorerApps() {
        _discoverState.update { DiscoverState.Fetching }
        _explorerApps.value = getExplorerProjects()
            .fold(
                onFailure = { error ->
                    _discoverState.update { DiscoverState.Failure(error) }
                    emptyList()
                },
                onSuccess = { it.toExplorerApp() }
            )
    }

    private fun List<Project>.toExplorerApp(): List<ExplorerApp> = map { project ->
        with(project) {
            ExplorerApp(id, name, homepage, imageId, description, imageUrl.toExplorerApp(), dappUrl, false)
        }
    }

    private fun WCImageUrl.toExplorerApp(): ImageUrl =
        ImageUrl(sm, md, lg)

    fun retryFetchingExplorerApps() {
        viewModelScope.launch {
            fetchExplorerApps()
        }
    }

    fun subscribeToDapp(explorerApp: ExplorerApp, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            _discoverState.update { DiscoverState.Subscribing(explorerApp) }
            val beforeSubscription = _activeSubscriptions.value

            Notify.Params.Subscribe(explorerApp.homepage.toUri(), with(EthAccountDelegate) { account.toEthAddress() }).let { subscribeParams ->
                NotifyClient.subscribe(
                    params = subscribeParams,
                    onSuccess = {
                        _activeSubscriptions.onEach { afterSubscription ->
                            if (beforeSubscription != afterSubscription) {
                                onSuccess()
                                _discoverState.update { DiscoverState.Fetched }
                                cancel()
                            }
                        }.launchIn(viewModelScope)
                    },
                    onError = {
                        _discoverState.update { DiscoverState.Fetched }
                        onFailure(it.throwable)
                    }
                )
            }
        }
    }

    fun unsubscribeFromDapp(explorerApp: ExplorerApp, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            if (explorerApp.topic == null) {
                onFailure(Throwable("Cannot unsubscribe from a dapp. Missing topic"))
                return@launch
            }

            _discoverState.update { DiscoverState.Unsubscribing(explorerApp) }
            val beforeSubscription = _activeSubscriptions.value

            Notify.Params.DeleteSubscription(explorerApp.topic).let { params ->
                NotifyClient.deleteSubscription(
                    params = params,
                    onSuccess = {
                        _activeSubscriptions.onEach { afterSubscription ->
                            if (beforeSubscription != afterSubscription) {
                                onSuccess()
                                _discoverState.update { DiscoverState.Fetched }
                                cancel()
                            }
                        }.launchIn(viewModelScope)
                    },
                    onError = {
                        _discoverState.update { DiscoverState.Fetched }
                        onFailure(it.throwable)
                    }
                )
            }
        }
    }
}

sealed interface SubscriptionsState {

    object Searching : SubscriptionsState
    data class Failure(val error: Throwable) : SubscriptionsState
    object Success : SubscriptionsState
    object Unsubscribed : SubscriptionsState

}


sealed interface DiscoverState {
    object Fetching : DiscoverState
    object Fetched : DiscoverState
    object Searching : DiscoverState
    data class Failure(val error: Throwable) : DiscoverState
    data class Subscribing(val explorerApp: ExplorerApp) : DiscoverState
    data class Unsubscribing(val explorerApp: ExplorerApp) : DiscoverState
}
