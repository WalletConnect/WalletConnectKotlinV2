package org.walletconnect.walletconnectv2.engine.serailising

interface JsonRpcSerialising {
    fun <T> trySerialize(typeClass: Class<T>, type: T): String
    fun <T> tryDeserialize(type: Class<T>, json: String): T?
}