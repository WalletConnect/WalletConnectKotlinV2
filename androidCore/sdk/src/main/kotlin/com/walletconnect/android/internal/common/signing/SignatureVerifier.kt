package com.walletconnect.android.internal.common.signing

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.eip1271.EIP1271Verifier
import com.walletconnect.android.internal.common.signing.eip191.EIP191Verifier
import com.walletconnect.android.internal.common.signing.signature.Signature

abstract class SignatureVerifier(private val projectId: ProjectId) {
    protected fun verify(signature: Signature, originalMessage: String, address: String, type: String): Boolean = when (type) {
        SignatureType.EIP191.header -> EIP191Verifier.verify(signature, originalMessage, address)
        SignatureType.EIP1271.header -> EIP1271Verifier.verify(signature, originalMessage, address, projectId.value)
        else -> throw RuntimeException("Invalid signature type")
    }
}