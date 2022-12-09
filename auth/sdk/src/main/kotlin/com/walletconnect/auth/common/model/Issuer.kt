@file:JvmSynthetic

package com.walletconnect.auth.common.model

internal data class Issuer(val value: String) {
    val chainId = "${value.split(ISS_DELIMITER)[ISS_POSITION_OF_NAMESPACE]}:${value.split(ISS_DELIMITER)[ISS_POSITION_OF_REFERENCE]}"
    val chainIdReference = value.split(ISS_DELIMITER)[ISS_POSITION_OF_REFERENCE]
    val address: String = value.split(ISS_DELIMITER)[ISS_POSITION_OF_ADDRESS]
    val accountId = "$chainId:$address"


    private companion object {
        const val ISS_DELIMITER = ":"
        const val ISS_POSITION_OF_NAMESPACE = 2
        const val ISS_POSITION_OF_REFERENCE = 3
        const val ISS_POSITION_OF_ADDRESS = 4
    }
}