package com.walletconnect.android.internal.common.cacao

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.cacao.eip1271.EIP1271Verifier
import com.walletconnect.android.internal.common.cacao.eip191.EIP191Verifier
import com.walletconnect.android.internal.common.model.ProjectId

class CacaoVerifier(private val projectId: ProjectId) {
    fun verify(cacao: Cacao): Boolean = when (cacao.signature.t) {
        SignatureType.EIP191.header -> EIP191Verifier.verify(
            cacao.signature.toSignature(),
            cacao.payload.toCAIP122Message(),
            Issuer(cacao.payload.iss).address
        )
        SignatureType.EIP1271.header -> EIP1271Verifier.verify(
            cacao.signature.toSignature(),
            cacao.payload.toCAIP122Message(),
            Issuer(cacao.payload.iss).address,
            projectId.value
        )
        else -> throw RuntimeException("Invalid header")
    }
}