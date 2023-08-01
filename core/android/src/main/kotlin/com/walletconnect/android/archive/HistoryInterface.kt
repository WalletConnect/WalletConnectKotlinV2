package com.walletconnect.android.archive

import com.walletconnect.android.Core
import com.walletconnect.android.archive.network.model.messages.MessagesParams
import com.walletconnect.android.internal.common.model.ArchiveMessage
import com.walletconnect.android.internal.common.model.Tags

interface HistoryInterface {
    fun initialize(relayServerUrl: String)

    suspend fun registerTags(tags: List<Tags>, onSuccess: () -> Unit, onError: (Core.Model.Error) -> Unit)

    suspend fun getAllMessages(params: MessagesParams, onSuccess: (List<ArchiveMessage>) -> Unit = {}, onError: (Core.Model.Error) -> Unit = {})

    companion object {
        const val DEFAULT_BATCH_SIZE = 200
    }
}