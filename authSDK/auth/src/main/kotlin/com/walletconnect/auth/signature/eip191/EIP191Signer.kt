package com.walletconnect.auth.signature.eip191

import com.walletconnect.auth.signature.Signature
import com.walletconnect.auth.signature.toSignature
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign


object EIP191Signer {
    fun sign(message: ByteArray, privateKey: ByteArray): Signature = Sign.signPrefixedMessage(message, ECKeyPair.create(privateKey)).toSignature()
    fun sign(message: String, privateKey: ByteArray): Signature = sign(message.toByteArray(), privateKey)
    fun signNoPrefix(message: ByteArray, privateKey: ByteArray): Signature = Sign.signMessage(message, ECKeyPair.create(privateKey)).toSignature()
    fun signNoPrefix(message: String, privateKey: ByteArray): Signature = signNoPrefix(message.toByteArray(), privateKey)
}