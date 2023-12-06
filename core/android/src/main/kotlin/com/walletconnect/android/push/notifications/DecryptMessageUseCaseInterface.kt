package com.walletconnect.android.push.notifications

import com.walletconnect.android.Core

interface DecryptMessageUseCaseInterface {
    suspend fun decryptNotification(topic: String, message: String, onSuccess: (Core.Model.Message) -> Unit, onFailure: (Throwable) -> Unit)
}