package com.walletconnect.android.echo

interface EchoInterface {

    fun register(firebaseAccessToken: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    fun unregister(onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    fun decryptMessage(topic: String, message: String, onSuccess: (String) -> Unit, onError: (Throwable) -> Unit)

    companion object {

        @JvmSynthetic
        internal const val KEY_CLIENT_ID = "clientId"
    }
}