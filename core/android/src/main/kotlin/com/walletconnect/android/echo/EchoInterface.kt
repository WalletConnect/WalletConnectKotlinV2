package com.walletconnect.android.echo

interface EchoInterface {
    val clientId: String

    fun register(firebaseAccessToken: String, enableEncrypted: Boolean? = false, onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    fun unregister(onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    companion object {

        @JvmSynthetic
        internal const val KEY_CLIENT_ID = "clientId"
    }
}