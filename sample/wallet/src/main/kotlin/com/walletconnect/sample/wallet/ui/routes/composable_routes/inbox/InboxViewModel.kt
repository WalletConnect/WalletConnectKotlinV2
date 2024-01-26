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
import com.walletconnect.sample.wallet.ui.common.ImageUrl
import com.walletconnect.sample.wallet.ui.common.subscriptions.ActiveSubscriptionsUI
import com.walletconnect.sample.wallet.ui.common.subscriptions.toUI
import com.walletconnect.sample.wallet.ui.routes.composable_routes.inbox.discover.ExplorerApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds
import com.walletconnect.android.internal.common.explorer.data.model.ImageUrl as WCImageUrl

class InboxViewModel(application: Application) : AndroidViewModel(application) {

    private val _subscriptionsState = MutableStateFlow<SubscriptionsState>(SubscriptionsState.Searching)
    val subscriptionsState = _subscriptionsState.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val subscriptionStateChangesEvents: StateFlow<List<Notify.Model.Subscription>> = NotifyDelegate.notifyEvents
        .filterIsInstance<Notify.Event.SubscriptionsChanged>()
        .debounce(500L)
        .map { event -> event.subscriptions }
        .stateIn(viewModelScope, SharingStarted.Eagerly, NotifyClient.getActiveSubscriptions(Notify.Params.GetActiveSubscriptions(EthAccountDelegate.ethAddress)).values.toList())

    private var _activeSubscriptions = emptyList<ActiveSubscriptionsUI>()
    private val getActiveSubscriptionTrigger = MutableSharedFlow<Unit>()

    private val _activeSubscriptionsFlow: Flow<List<ActiveSubscriptionsUI>> = merge(
        subscriptionStateChangesEvents.onEach { _activeSubscriptions = it.toUI() },
        getActiveSubscriptionTrigger.onEach { _activeSubscriptions = NotifyClient.getActiveSubscriptions(Notify.Params.GetActiveSubscriptions(EthAccountDelegate.ethAddress)).values.toList().toUI() }
    ).map { _activeSubscriptions }

    private val _activeSubscriptionsTrigger = MutableSharedFlow<Unit>()

    val activeSubscriptions = _activeSubscriptionsTrigger
        .debounce(500L)
        .filter { _subscriptionsState.value !is SubscriptionsState.Failure }
        .onEach { _subscriptionsState.update { SubscriptionsState.Searching } }
        .combine(_activeSubscriptionsFlow) { _, activeSubscriptions ->
            val text = searchText.value
            if (text.isBlank()) {
                activeSubscriptions.toList()
            } else {
                activeSubscriptions.filter { it.doesMatchSearchQuery(text) }
            }.also {
                if (activeSubscriptions.isEmpty()) _subscriptionsState.update { SubscriptionsState.Unsubscribed }
                else _subscriptionsState.update { SubscriptionsState.Success }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _activeSubscriptions)

    fun onSearchTextChange(text: String) {
        _searchText.value = text
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
        .combine(_activeSubscriptionsFlow) { projects, activeSubscriptions ->
            projects.map { explorerApp ->
                activeSubscriptions.find { it.appDomain == explorerApp.dappUrl }?.let {
                    explorerApp.copy(isSubscribed = true, topic = it.topic)
                } ?: explorerApp
            }
        }
        .onEach { _discoverState.update { DiscoverState.Fetched } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _explorerApps.value)

    init {
        searchText
            .onEach {
                _explorerAppsTrigger.emit(Unit)
                _activeSubscriptionsTrigger.emit(Unit)
            }
            .launchIn(viewModelScope)

        _activeSubscriptionsFlow
            .onEach { _activeSubscriptionsTrigger.emit(Unit) }
            .launchIn(viewModelScope)

        _explorerApps
            .onEach { _explorerAppsTrigger.emit(Unit) }
            .launchIn(viewModelScope)

        viewModelScope.launch { fetchExplorerApps() }
    }

    private suspend fun fetchExplorerApps() {
        _discoverState.update { DiscoverState.Fetching }
        _explorerApps.value = getExplorerProjects()
            .fold(
                onFailure = { error ->
                    Timber.e(error)
                    _discoverState.update { DiscoverState.Failure(error) }
                    emptyList()
                },
                onSuccess = { it.toUI() }
            )
    }

    suspend fun fetchActiveSubscriptions() {
        getActiveSubscriptionTrigger.emit(Unit)
    }

    private fun List<Project>.toUI(): List<ExplorerApp> = map { project ->
        with(project) {
            ExplorerApp(id, name, homepage, imageId, description, imageUrl.toUI(), dappUrl, false)
        }
    }

    private fun WCImageUrl.toUI(): ImageUrl = ImageUrl(sm, md, lg)

    fun retryFetchingExplorerApps() {
        viewModelScope.launch {
            fetchExplorerApps()
        }
    }

    fun subscribeToDapp(explorerApp: ExplorerApp, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _discoverState.update { DiscoverState.Subscribing(explorerApp) }

            val subscribeParams = Notify.Params.Subscribe(explorerApp.dappUrl.toUri(), EthAccountDelegate.ethAddress, 5.seconds)

            for (i in 0..1) {
                viewModelScope.launch(Dispatchers.IO) {
                    NotifyClient.subscribe(params = subscribeParams).let { result ->
                        when (result) {
                            is Notify.Result.Subscribe.Success -> {
                                fetchActiveSubscriptions()
                                onSuccess()
                            }

                            is Notify.Result.Subscribe.Error -> {
                                Timber.e(result.error.throwable)
                                onFailure(result.error.throwable)
                            }
                        }
                        _discoverState.update { DiscoverState.Fetched }
                    }
                }
            }
        }
    }


    fun unsubscribeFromDapp(explorerApp: ExplorerApp, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (explorerApp.topic == null) {
                val error = Throwable("Cannot unsubscribe from a dapp. Missing topic")
                Timber.e(error)
                return@launch onFailure(error)
            }

            _discoverState.update { DiscoverState.Unsubscribing(explorerApp) }

            Notify.Params.DeleteSubscription(explorerApp.topic).let { params ->
                NotifyClient.deleteSubscription(params = params).let { result ->
                    when (result) {
                        is Notify.Result.DeleteSubscription.Success -> {
                            fetchActiveSubscriptions()
                            _discoverState.update { DiscoverState.Fetched }
                            onSuccess()
                        }

                        is Notify.Result.DeleteSubscription.Error -> {
                            _discoverState.update { DiscoverState.Fetched }
                            Timber.e(result.error.throwable)
                            onFailure(result.error.throwable)
                        }
                    }
                    _discoverState.update { DiscoverState.Fetched }
                }
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
