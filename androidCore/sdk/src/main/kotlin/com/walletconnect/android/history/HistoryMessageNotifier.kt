package com.walletconnect.android.history

import com.walletconnect.foundation.network.model.Relay
import kotlinx.coroutines.flow.MutableSharedFlow


internal class HistoryMessageNotifier {
    val requestsSharedFlow = MutableSharedFlow<Relay.Model.Call.Subscription.Request>()
}