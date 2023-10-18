@file:OptIn(FlowPreview::class)

package com.walletconnect.sample.wallet.ui.routes.composable_routes.explorer_dapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.explorer.data.model.Project
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.walletconnect.android.internal.common.explorer.data.model.ImageUrl as WCImageUrl

class ExploreDappsViewModel : ViewModel() {
    private suspend fun getExplorerProjects() = CoreClient.Explorer.getProjects(0, 500, false)


    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _state = MutableStateFlow<ExplorerState>(ExplorerState.Searching)
    val state = _state.asStateFlow()

    private val _projects = MutableStateFlow(emptyList<ExplorerApp>())
    val projects = searchText
        .debounce(500L)
        .filter { _state.value !is ExplorerState.Failure }
        .onEach { _state.update { ExplorerState.Searching } }
        .combine(_projects) { text, projects ->
            if (text.isBlank()) {
                projects
            } else {
                projects.filter { it.doesMatchSearchQuery(text) }
            }
        }
        .onEach { _state.update { ExplorerState.Success } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _projects.value)


    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    init {
        viewModelScope.launch {
            fetchExplorerApps()
        }
    }

    private suspend fun fetchExplorerApps() {
        _projects.value = getExplorerProjects().fold(onFailure = { error -> _state.update { ExplorerState.Failure(error) }; emptyList() }, onSuccess = { it.toExplorerApp() })
    }

    private fun List<Project>.toExplorerApp(): List<ExplorerApp> = map { project ->
        with(project) {
            ExplorerApp(id, name, homepage, imageId, description, imageUrl.toExplorerApp(), dappUrl)
        }
    }

    private fun WCImageUrl.toExplorerApp(): ImageUrl = ImageUrl(sm, md, lg)

}

sealed interface ExplorerState {
    object Searching : ExplorerState
    data class Failure(val error: Throwable) : ExplorerState
    object Success : ExplorerState
}