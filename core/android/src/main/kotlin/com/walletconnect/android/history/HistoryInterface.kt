package com.walletconnect.android.history

import com.walletconnect.android.Core
import com.walletconnect.android.history.network.model.messages.MessagesParams
import com.walletconnect.android.internal.common.model.HistoryMessage
import com.walletconnect.android.internal.common.model.Tags

interface HistoryInterface {
    fun initialize(relayServerUrl: String)

    suspend fun registerTags(tags: List<Tags>, onSuccess: () -> Unit, onError: (Core.Model.Error) -> Unit)

    suspend fun getMessages(params: MessagesParams, onSuccess: (List<HistoryMessage>) -> Unit = {}, onError: (Core.Model.Error) -> Unit = {})
}