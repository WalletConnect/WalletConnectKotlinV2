package com.walletconnect.android.archive

import com.walletconnect.android.Core
import com.walletconnect.android.archive.network.model.messages.MessagesParams
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.sync.ClientJsonRpc
import com.walletconnect.android.internal.common.model.type.ClientParams

interface ArchiveInterface {
    fun initialize(relayServerUrl: String)

    suspend fun registerTags(tags: List<Tags>, onSuccess: () -> Unit, onError: (Core.Model.Error) -> Unit)

    suspend fun getAllMessages(params: MessagesParams, onSuccess: suspend (Map<ClientJsonRpc, ClientParams>) -> Unit = {}, onError: (Core.Model.Error) -> Unit = {})

    companion object {
        const val DEFAULT_BATCH_SIZE = 200
    }
}