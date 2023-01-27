@file:JvmSynthetic

package com.walletconnect.android.internal.common.cacao

data class Issuer(val value: String) {
    val chainId
        get() = "${value.split(ISS_DELIMITER)[ISS_POSITION_OF_NAMESPACE]}:${value.split(ISS_DELIMITER)[ISS_POSITION_OF_REFERENCE]}"
    val chainIdReference
        get() = value.split(ISS_DELIMITER)[ISS_POSITION_OF_REFERENCE]
    val address: String
        get() = value.split(ISS_DELIMITER)[ISS_POSITION_OF_ADDRESS]
    val accountId
        get() = "$chainId:$address"

    private companion object {
        const val ISS_DELIMITER = ":"
        const val ISS_POSITION_OF_NAMESPACE = 2
        const val ISS_POSITION_OF_REFERENCE = 3
        const val ISS_POSITION_OF_ADDRESS = 4
    }
}