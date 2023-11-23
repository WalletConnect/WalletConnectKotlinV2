package com.walletconnect.android.echo

interface DecryptMessageUseCaseInterface {
    suspend fun decryptMessage(topic: String, message: String, onSuccess: (Message) -> Unit, onFailure: (Throwable) -> Unit)
}