package com.walletconnect.android.internal.common.cacao.signature

import com.walletconnect.android.internal.common.cacao.guaranteeNoHexPrefix
import com.walletconnect.util.hexToBytes
import com.walletconnect.util.bytesToHex
import com.walletconnect.utils.HexPrefix
import org.web3j.crypto.Sign


internal fun Sign.SignatureData.toSignature(): Signature = Signature(v, r, s)
fun Signature.toCacaoSignature(): String = String.HexPrefix + r.bytesToHex() + s.bytesToHex() + v.bytesToHex()
internal fun Signature.toSignatureData(): Sign.SignatureData = Sign.SignatureData(v, r, s)


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



