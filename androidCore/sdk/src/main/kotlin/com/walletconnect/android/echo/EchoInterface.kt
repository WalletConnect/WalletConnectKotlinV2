package com.walletconnect.android.echo

interface EchoInterface {

    fun initialize()

    fun register(firebaseAccessToken: String)

    fun unregister()

    fun decryptMessage(topic: String, message: String): String
}