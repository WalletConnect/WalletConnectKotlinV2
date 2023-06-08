package com.walletconnect.android.internal.common.signing.eip191

import com.walletconnect.android.internal.common.signing.model.HexString
import com.walletconnect.android.internal.common.signing.signature.Signature
import com.walletconnect.android.internal.common.signing.signature.toSignature
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric.hexStringToByteArray


object EIP191Signer {
    fun sign(message: ByteArray, privateKey: ByteArray): Signature = Sign.signPrefixedMessage(message, ECKeyPair.create(privateKey)).toSignature()
    fun sign(message: String, privateKey: ByteArray): Signature = sign(message.toByteArray(), privateKey)
    fun signHex(message: HexString, privateKey: ByteArray): Signature = sign(hexStringToByteArray(message.value), privateKey)
    fun signNoPrefix(message: ByteArray, privateKey: ByteArray): Signature = Sign.signMessage(message, ECKeyPair.create(privateKey)).toSignature()
    fun signNoPrefix(message: String, privateKey: ByteArray): Signature = signNoPrefix(message.toByteArray(), privateKey)
    fun signHexNoPrefix(message: HexString, privateKey: ByteArray): Signature = signNoPrefix(hexStringToByteArray(message.value), privateKey)
}