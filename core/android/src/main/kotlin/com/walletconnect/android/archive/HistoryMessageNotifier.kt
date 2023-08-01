package com.walletconnect.android.archive

import com.walletconnect.foundation.network.model.Relay
import kotlinx.coroutines.flow.MutableSharedFlow


internal class HistoryMessageNotifier {
    val requestsSharedFlow = MutableSharedFlow<Relay.Model.Call.Subscription.Request>()
}