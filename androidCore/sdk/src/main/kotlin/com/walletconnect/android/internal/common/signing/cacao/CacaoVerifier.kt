package com.walletconnect.android.internal.common.signing.cacao

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.signature.verify

class CacaoVerifier(private val projectId: ProjectId) {
    fun verify(cacao: Cacao): Boolean = when (cacao.signature.t) {
        SignatureType.EIP191.header, SignatureType.EIP1271.header ->
            cacao.signature.toSignature().verify(cacao.payload.toCAIP122Message(), Issuer(cacao.payload.iss).address, cacao.signature.t, projectId)
        else -> throw RuntimeException("Invalid header")
    }
}

