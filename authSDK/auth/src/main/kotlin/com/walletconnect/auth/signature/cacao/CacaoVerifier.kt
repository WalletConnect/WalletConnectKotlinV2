package com.walletconnect.auth.signature.cacao

import com.walletconnect.auth.common.model.CacaoVO
import com.walletconnect.auth.engine.model.mapper.toFormattedMessage
import com.walletconnect.auth.engine.model.mapper.toSignature
import com.walletconnect.auth.signature.SignatureType
import com.walletconnect.auth.signature.eip191.EIP191Verifier

internal object CacaoVerifier {
    fun verify(cacao: CacaoVO): Boolean = when (cacao.header.t) {
        SignatureType.EIP191.header -> EIP191Verifier.verify(cacao.signature.toSignature(), cacao.payload.toFormattedMessage(), cacao.payload.address)
        else -> false // todo: Add unsupported types handling
    }
}