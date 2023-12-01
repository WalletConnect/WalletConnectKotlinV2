package com.walletconnect.web3.modal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.walletconnect.web3.modal.ui.components.internal.snackbar.LocalSnackBarHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
internal fun ConsumeNavigationEventsEffect(
    navController: NavController,
    navigator: Navigator,
    closeModal: (() -> Unit)? = null
) {
    val snackBar = LocalSnackBarHandler.current
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        navigator.navigationEvents
            .onEach {
                when (it) {
                    is NavigationEvent.Navigate -> navController.navigate(it.path)
                    is NavigationEvent.PopBackStack -> navController.popBackStack()
                    is NavigationEvent.PopBackStackTo -> navController.popBackStack(it.path, it.inclusive)
                    is NavigationEvent.Close -> closeModal?.invoke()
                    is NavigationEvent.ShowError -> { snackBar.showErrorSnack(it.message ?: "Something went wrong") }
                }
                if (navController.currentDestination == null) {
                    closeModal?.invoke()
                }
            }
            .launchIn(coroutineScope)
    }
}

sealed class NavigationEvent {
    data class Navigate(val path: String) : NavigationEvent()
    object PopBackStack : NavigationEvent()

    data class PopBackStackTo(val path: String, val inclusive: Boolean) : NavigationEvent()

    object Close : NavigationEvent()

    data class ShowError(val message: String? = null) : NavigationEvent()
}

interface Navigator {

    val navigationEvents: SharedFlow<NavigationEvent>
    fun navigateTo(path: String)

    fun popBackStack()

    fun popBackStack(path: String, inclusive: Boolean)

    fun closeModal()

    fun showError(message: String? = null)
}

internal class NavigatorImpl : Navigator {

    private val _navEventsFlow = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)

    override val navigationEvents = _navEventsFlow.asSharedFlow()

    override fun popBackStack() {
        _navEventsFlow.tryEmit(NavigationEvent.PopBackStack)
    }

    override fun popBackStack(path: String, inclusive: Boolean) {
        _navEventsFlow.tryEmit(NavigationEvent.PopBackStackTo(path, inclusive))
    }

    override fun navigateTo(path: String) {
        _navEventsFlow.tryEmit(NavigationEvent.Navigate(path))
    }

    override fun closeModal() {
        _navEventsFlow.tryEmit(NavigationEvent.Close)
    }

    override fun showError(message: String?) {
        _navEventsFlow.tryEmit(NavigationEvent.ShowError(message))
    }
}
