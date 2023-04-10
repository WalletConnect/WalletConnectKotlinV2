package com.walletconnect.android.internal.common.signing.cacao

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.SignatureVerifier


class CacaoVerifier(projectId: ProjectId) : SignatureVerifier(projectId) {
    fun verify(cacao: Cacao): Boolean = when (cacao.signature.t) {
        SignatureType.EIP191.header, SignatureType.EIP1271.header -> super.verify(
            cacao.signature.toSignature(),
            cacao.payload.toCAIP122Message(),
            Issuer(cacao.payload.iss).address,
            cacao.signature.t
        )
        else -> throw RuntimeException("Invalid header")
    }
}

