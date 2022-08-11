package com.walletconnect.auth.signature

import com.walletconnect.util.hexToBytes

data class Signature(val v: ByteArray, val r: ByteArray, val s: ByteArray) {
    companion object {
        fun fromString(string: String): Signature = string.guaranteeNoHexPrefix().let { noPrefix ->
            val r = noPrefix.substring(0, 64).hexToBytes()
            val s = noPrefix.substring(64, 128).hexToBytes()
            val iv = Integer.parseUnsignedInt(noPrefix.substring(128, 130), 16).also { iv -> if (iv < 27) iv + 27 }
            val v = Integer.toHexString(iv).hexToBytes()

            Signature(v = v, r = r, s = s)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as Signature
        if (!v.contentEquals(that.v)) {
            return false
        }
        return if (!r.contentEquals(that.r)) {
            false
        } else s.contentEquals(that.s)
    }

    override fun hashCode(): Int {
        var result = v.contentHashCode()
        result = 31 * result + r.contentHashCode()
        result = 31 * result + s.contentHashCode()
        return result
    }
}

