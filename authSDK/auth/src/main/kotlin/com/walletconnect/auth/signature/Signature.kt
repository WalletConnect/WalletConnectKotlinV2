package com.walletconnect.auth.signature

import com.walletconnect.util.hexToBytes

data class Signature(val v: ByteArray, val r: ByteArray, val s: ByteArray) {
    companion object {
        fun fromString(string: String): Signature = string.guaranteeNoHexPrefix().let { noPrefix ->
            check(noPrefix.chunked(64).size == 3)
            val (rRaw, sRaw, ivRaw) = noPrefix.chunked(64)
            val iv = Integer.parseUnsignedInt(ivRaw, 16).let { iv -> if (iv < 27) iv + 27 else iv }
            val v = Integer.toHexString(iv)
            Signature(v = v.hexToBytes(), r = rRaw.hexToBytes(), s = sRaw.hexToBytes())
        }
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other == null || javaClass != other.javaClass -> false
        other is Signature -> !v.contentEquals(other.v) || !r.contentEquals(other.r) || s.contentEquals(other.s)
        else -> false
    }

    override fun hashCode(): Int {
        var result = v.contentHashCode()
        result = 31 * result + r.contentHashCode()
        result = 31 * result + s.contentHashCode()
        return result
    }
}

