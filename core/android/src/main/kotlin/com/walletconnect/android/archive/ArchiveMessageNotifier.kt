package com.walletconnect.android.archive

import com.walletconnect.foundation.network.model.Relay
import kotlinx.coroutines.flow.MutableSharedFlow


internal class ArchiveMessageNotifier {
    val requestsSharedFlow = MutableSharedFlow<Relay.Model.Call.Subscription.Request>()
}