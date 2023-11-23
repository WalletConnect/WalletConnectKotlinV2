package com.walletconnect.android.echo

interface EchoInterface {
    val clientId: String

    //todo: expose flag
    fun register(firebaseAccessToken: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    fun unregister(onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    //todo
    //fun decryptMessage(): Message
    //fun getNotificationMetadata(): Notification

    companion object {

        @JvmSynthetic
        internal const val KEY_CLIENT_ID = "clientId"
    }
}