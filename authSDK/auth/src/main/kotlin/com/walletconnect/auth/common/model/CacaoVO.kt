@file:JvmSynthetic
package com.walletconnect.auth.common.model


internal data class CacaoVO(
    val header: HeaderVO,
    val payload: PayloadVO,
    val signature: SignatureVO,
) {
    data class SignatureVO(val t: String, val s: String, val m: String? = null)
    data class HeaderVO(val t: String)
    data class PayloadVO(
        val iss: String,
        val domain: String,
        val aud: String,
        val version: String,
        val nonce: String,
        val iat: String,
        val nbf: String?,
        val exp: String?,
        val statement: String?,
        val requestId: String?,
        val resources: List<String>?,
    ) {
        val address: String
        val chainId: String

        init {
            iss.split(ISS_DELIMITER).apply {
                address = get(ISS_POSITION_OF_ADDRESS)
                chainId = get(ISS_POSITION_OF_CHAIN_ID)
            }
        }

        private companion object {
            const val ISS_DELIMITER = ":"
            const val ISS_POSITION_OF_CHAIN_ID = 3
            const val ISS_POSITION_OF_ADDRESS = 4
        }
    }
}
