package com.walletconnect.auth.signature.eip191

import com.walletconnect.auth.signature.Signature
import com.walletconnect.util.hexToBytes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys

internal class EIP191SignerTest {

    private val message = "Message"
    private val privateKey = "305c6cde3846927892cd32762f6120539f3ec74c9e3a16b9b798b1e85351ae2a".hexToBytes()

    @Test
    fun signAndVerify() {
        val address = Keys.getAddress(ECKeyPair.create(privateKey))
        val signature = EIP191Signer.sign(message, privateKey)
        val result = EIP191Verifier.verify(signature, message, address)
        assertTrue(result)
    }

    @Test
    fun signAndVerifyNoPrefix() {
        val address = Keys.getAddress(ECKeyPair.create(privateKey))
        val signature = EIP191Signer.signNoPrefix(message, privateKey)
        val result = EIP191Verifier.verifyNoPrefix(signature, message, address)
        assertTrue(result)
    }

    private val addressFromEtherscan = "0x95967c59dbeb5a473bc2e1b3232a2d2fe8532c4f"
    private val signedMessageFromEtherscan =
        "Hello, this is Yanzi#1853 and I am requesting the OG role. I minted in this transaction: https://etherscan.io/tx/0xba54fee40f715809ff365cde3ba143f9cd3cee0515e5d4e4640f9db5664ff600"
    private val signatureHashFromEtherscan = "0xc89c3b1c82b0a518bad7deb31ca523c0150b5c764fb26c27149b5d46a1ffd5901466c0d9bcc6e1974abe0ba124e8ce821d436023d19262d53ea2812492a19d3c1c"


    @Test
    fun etherscanVerify() {
        val address = addressFromEtherscan
        val ogMessage = signedMessageFromEtherscan.toByteArray()
        val messageHash = Signature.fromString(signatureHashFromEtherscan)
        val result = EIP191Verifier.verify(messageHash, ogMessage, address)
        assertTrue(result)
    }

    @Test
    fun etherscanVerifyNoSignPrefix() {
        val address = addressFromEtherscan
        val ogMessage = signedMessageFromEtherscan.toByteArray()
        val messageHash = Signature.fromString(signatureHashFromEtherscan)
        val result = EIP191Verifier.verifyNoPrefix(messageHash, ogMessage, address)
        assertFalse(result)
    }
}
