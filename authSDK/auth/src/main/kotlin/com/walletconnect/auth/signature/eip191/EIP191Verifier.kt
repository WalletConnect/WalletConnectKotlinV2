package com.walletconnect.auth.signature.eip191

import com.walletconnect.auth.signature.Signature
import com.walletconnect.auth.signature.guaranteeNoHexPrefix
import com.walletconnect.auth.signature.toSignatureData
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import java.security.SignatureException

object EIP191Verifier {
    fun verify(signature: Signature, originalMessage: ByteArray, address: String): Boolean =
        getAddressUsedToSignPrefixedMessage(signature.toSignatureData(), originalMessage).equals(address.guaranteeNoHexPrefix(), ignoreCase = true)

    fun verify(signature: Signature, originalMessage: String, address: String): Boolean = verify(signature, originalMessage.toByteArray(), address)

    fun verifyNoPrefix(signature: Signature, originalMessage: ByteArray, address: String): Boolean =
        getAddressUsedToSignMessage(signature.toSignatureData(), originalMessage).also { println(it) }.equals(address.guaranteeNoHexPrefix(), ignoreCase = true)

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