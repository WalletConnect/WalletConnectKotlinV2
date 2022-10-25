package com.walletconnect.auth.signature.cacao

import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.auth.common.model.Cacao
import com.walletconnect.auth.engine.mapper.toFormattedMessage
import com.walletconnect.auth.engine.mapper.toSignature
import com.walletconnect.auth.signature.SignatureType
import com.walletconnect.auth.signature.eip1271.EIP1271Verifier
import com.walletconnect.auth.signature.eip191.EIP191Verifier

internal class CacaoVerifier(private val projectId: ProjectId) {
    fun verify(cacao: Cacao): Boolean = when (cacao.signature.t) {
        SignatureType.EIP191.header -> EIP191Verifier.verify(cacao.signature.toSignature(), cacao.payload.toFormattedMessage(), cacao.payload.address)
        SignatureType.EIP1271.header -> EIP1271Verifier.verify(cacao.signature.toSignature(), cacao.payload.toFormattedMessage(), cacao.payload.address, projectId.value)
        else -> throw RuntimeException("Invalid header")
    }
}