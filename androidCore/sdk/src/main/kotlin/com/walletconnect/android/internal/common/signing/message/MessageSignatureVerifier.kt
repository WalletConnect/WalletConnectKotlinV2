package com.walletconnect.android.internal.common.signing.message

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.signature.Signature
import com.walletconnect.android.internal.common.signing.signature.verify


class MessageSignatureVerifier(private val projectId: ProjectId) {
    fun verify(signature: String, originalMessage: String, address: String, type: SignatureType) {
        Signature.fromString(signature).verify(originalMessage, address, type.header, projectId)
    }
}