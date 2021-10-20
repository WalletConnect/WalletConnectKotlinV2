package org.walletconnect.walletconnectv2.crypto

interface KeyChain {

    fun setKey(key: String, value: String)

    fun getKey(key: String): String
}