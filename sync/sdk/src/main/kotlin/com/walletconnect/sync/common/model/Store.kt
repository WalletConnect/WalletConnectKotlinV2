@file:JvmSynthetic

package com.walletconnect.sync.common.model

import com.walletconnect.util.bytesToInt

@JvmInline
internal value class Store(val value: String) {

    companion object {
        const val STORE_BASE_PATH = "m/77'/0'/0"
        const val PATH_DELIMITER = "/"
    }

    fun getDerivationPath(basePath: String = STORE_BASE_PATH) = basePath + PATH_DELIMITER + toPath()

    fun toPath(): String = this.value
        .chunked(4)
        .map { chunk ->
            val bytes = ByteArray(chunk.length)
            chunk.mapIndexed { index, char ->
                bytes[index] = char.code.toByte()
            }
            bytes.bytesToInt(chunk.length)
        }
        .joinToString(PATH_DELIMITER)
}

