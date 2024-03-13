package com.walletconnect.android.internal.common.signing.cacao

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.signature.verify
import org.web3j.utils.Numeric

class CacaoVerifier(private val projectId: ProjectId) {
    fun verify(cacao: Cacao): Boolean = when (cacao.signature.t) {

        SignatureType.EIP191.header, SignatureType.EIP1271.header -> {
            val plainMessage = cacao.payload.toCAIP222Message()
            val hexMessage = Numeric.toHexString(cacao.payload.toCAIP222Message().toByteArray())
            val address = Issuer(cacao.payload.iss).address

            if (cacao.signature.toSignature().verify(plainMessage, address, cacao.signature.t, projectId)) {
                true
            } else {
                cacao.signature.toSignature().verify(hexMessage, address, cacao.signature.t, projectId)
            }
        }

        else -> throw RuntimeException("Invalid header")
    }
}

