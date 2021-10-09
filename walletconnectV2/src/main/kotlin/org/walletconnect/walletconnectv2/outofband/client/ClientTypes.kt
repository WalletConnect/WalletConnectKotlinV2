package org.walletconnect.walletconnectv2.outofband.client

sealed class ClientTypes {

    data class InitialParams(val useTls: Boolean, val hostName: String, val port: UInt, val apiKey: String)

    data class PairParams(val uri: String): ClientTypes()
}
