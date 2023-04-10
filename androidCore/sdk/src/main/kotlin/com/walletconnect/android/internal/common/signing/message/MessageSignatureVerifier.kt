package com.walletconnect.android.internal.common.signing.message

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.SignatureVerifier
import com.walletconnect.android.internal.common.signing.signature.Signature

class MessageSignatureVerifier(projectId: ProjectId) : SignatureVerifier(projectId) {
    fun verify(signature: String, originalMessage: String, address: String, type: SignatureType) {
        super.verify(Signature.fromString(signature), originalMessage, address, type.header)
    }
}