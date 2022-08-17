@file:JvmSynthetic

package com.walletconnect.auth.signature.cacao

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.signature.SignatureType
import com.walletconnect.auth.signature.eip191.EIP191Signer
import com.walletconnect.auth.signature.toCacaoSignature
import org.web3j.crypto.ECKeyPair

class CacaoSigner {
    private val eip191Signer = EIP191Signer()

    fun sign(message: ByteArray, keyPair: ECKeyPair, type: SignatureType): Auth.Model.Cacao.Signature = when (type) {
        SignatureType.EIP191 -> Auth.Model.Cacao.Signature(type.header, eip191Signer.sign(message, keyPair).toCacaoSignature())
    }

    fun sign(message: String, keyPair: ECKeyPair, type: SignatureType): Auth.Model.Cacao.Signature = sign(message.toByteArray(), keyPair, type)
}