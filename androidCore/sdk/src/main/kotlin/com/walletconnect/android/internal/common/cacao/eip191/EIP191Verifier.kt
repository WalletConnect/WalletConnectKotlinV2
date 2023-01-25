package com.walletconnect.android.internal.common.cacao.eip191

import com.walletconnect.android.internal.common.cacao.guaranteeNoHexPrefix
import com.walletconnect.android.internal.common.cacao.signature.Signature
import com.walletconnect.android.internal.common.cacao.signature.toSignatureData
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import java.security.SignatureException

internal object EIP191Verifier {
    fun verify(signature: Signature, originalMessage: ByteArray, address: String): Boolean =
        getAddressUsedToSignPrefixedMessage(signature.toSignatureData(), originalMessage).equals(address.guaranteeNoHexPrefix(), ignoreCase = true)

    fun verify(signature: Signature, originalMessage: String, address: String): Boolean = verify(signature, originalMessage.toByteArray(), address)

    fun verifyNoPrefix(signature: Signature, originalMessage: ByteArray, address: String): Boolean =
        getAddressUsedToSignMessage(signature.toSignatureData(), originalMessage).equals(address.guaranteeNoHexPrefix(), ignoreCase = true)

    fun verifyNoPrefix(signature: Signature, originalMessage: String, address: String): Boolean = verifyNoPrefix(signature, originalMessage.toByteArray(), address)

    @Throws(SignatureException::class)
    private fun getAddressUsedToSignPrefixedMessage(signedHash: Sign.SignatureData, originalMessage: ByteArray): String {
        return Keys.getAddress(Sign.signedPrefixedMessageToKey(originalMessage, signedHash).toString(16))
    }

    @Throws(SignatureException::class)
    private fun getAddressUsedToSignMessage(signedHash: Sign.SignatureData, originalMessage: ByteArray): String {
        return Keys.getAddress(Sign.signedMessageToKey(originalMessage, signedHash).toString(16))
    }
}