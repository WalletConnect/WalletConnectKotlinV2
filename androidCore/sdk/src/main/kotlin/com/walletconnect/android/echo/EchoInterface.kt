package com.walletconnect.android.echo

interface EchoInterface {

    fun initialize()

    fun register(firebaseAccessToken: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    fun unregister()

    fun decryptMessage(topic: String, message: String): String

    companion object {

        @JvmSynthetic
        internal const val KEY_CLIENT_ID = "clientId"
    }
}