package com.walletconnect.sample.wallet.ui.routes.composable_routes.notifications

import androidx.lifecycle.ViewModel
import com.walletconnect.sample.wallet.domain.model.PushNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationsViewModel : ViewModel() {
//    private val notificationsState = PushWalletDelegate.wcPushEventModels.map { pushEvent ->
//        when(pushEvent) {
//            else -> {}
//        }
//    }

    private val _notificationsStateFlow = MutableStateFlow<NotificationsState>(NotificationsState.Empty)
    val notificationsState = _notificationsStateFlow.asStateFlow()
}

sealed class NotificationsState {
    object Empty: NotificationsState()
    data class Success(val notifications: List<PushNotification>): NotificationsState()
    object Error: NotificationsState()
}