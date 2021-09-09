package org.walletconnect.walletconnectv2.outofband

sealed class ClientTypes {

    data class PairParams(val uri: String): ClientTypes()
}
