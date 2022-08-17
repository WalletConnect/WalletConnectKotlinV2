package com.walletconnect.auth.signature.cacao

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.signature.SignatureType
import com.walletconnect.auth.signature.eip191.EIP191Verifier
import com.walletconnect.auth.signature.toFormattedMessage
import com.walletconnect.auth.signature.toSignature

class CacaoVerifier {
    private val eip191Verifier = EIP191Verifier()

    fun verify(cacao: Auth.Model.Cacao): Boolean = when (cacao.header.t) {
        SignatureType.EIP191.header -> eip191Verifier.verify(cacao.signature.toSignature(), cacao.payload.toFormattedMessage(), cacao.payload.address)
        else -> TODO("Only ${SignatureType.EIP191.header} type is implemented")
    }
}