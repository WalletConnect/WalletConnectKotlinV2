@file:JvmSynthetic

package com.walletconnect.auth.signature.cacao

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.signature.SignatureType
import com.walletconnect.auth.signature.eip191.EIP191Signer
import com.walletconnect.auth.signature.toCacaoSignature

object CacaoSigner {
    fun sign(message: ByteArray, privateKey: ByteArray, type: SignatureType): Auth.Model.Cacao.Signature = when (type) {
        SignatureType.EIP191 -> Auth.Model.Cacao.Signature(type.header, EIP191Signer.sign(message, privateKey).toCacaoSignature())
    }

    fun sign(message: String, privateKey: ByteArray, type: SignatureType): Auth.Model.Cacao.Signature = sign(message.toByteArray(), privateKey, type)
}