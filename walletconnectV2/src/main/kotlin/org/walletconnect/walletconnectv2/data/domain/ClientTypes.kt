package org.walletconnect.walletconnectv2.data.domain

sealed class ClientTypes {

    data class PairParams(val uri: String): ClientTypes()
}
