package com.walletconnect.auth.signature.eip191

import com.walletconnect.auth.signature.Signature
import com.walletconnect.auth.signature.toSignature
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign


class EIP191Signer {
    fun sign(message: ByteArray, keyPair: ECKeyPair): Signature = Sign.signPrefixedMessage(message, keyPair).toSignature()
    fun sign(message: String, keyPair: ECKeyPair): Signature = sign(message.toByteArray(), keyPair)
    fun signNoPrefix(message: ByteArray, keyPair: ECKeyPair): Signature = Sign.signMessage(message, keyPair).toSignature()
    fun signNoPrefix(message: String, keyPair: ECKeyPair): Signature = signNoPrefix(message.toByteArray(), keyPair)
}