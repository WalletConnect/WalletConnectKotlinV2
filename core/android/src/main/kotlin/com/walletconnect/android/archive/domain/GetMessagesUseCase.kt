package com.walletconnect.android.archive.domain

import com.walletconnect.android.archive.network.HistoryServerService
import com.walletconnect.android.archive.network.model.messages.MessagesParams
import com.walletconnect.android.archive.network.model.messages.MessagesResponse
import com.walletconnect.foundation.util.Logger

class GetMessagesUseCase(
    private val service: HistoryServerService,
    private val logger: Logger,
) {
    suspend operator fun invoke(params: MessagesParams): Result<MessagesResponse> = runCatching {
        with(service.messages(queryMap = params.toQueryMap())) {
            if (isSuccessful && body() != null) {
                return Result.success(body()!!)
            } else {
                throw Throwable(errorBody()?.string())
            }
        }
    }
}

