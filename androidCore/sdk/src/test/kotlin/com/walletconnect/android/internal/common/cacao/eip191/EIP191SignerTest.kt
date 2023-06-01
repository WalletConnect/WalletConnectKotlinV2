package com.walletconnect.android.internal.common.cacao.eip191

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.toCAIP122Message
import com.walletconnect.android.internal.common.signing.cacao.toSignature
import com.walletconnect.android.internal.common.signing.eip191.EIP191Signer
import com.walletconnect.android.internal.common.signing.eip191.EIP191Verifier
import com.walletconnect.android.internal.common.signing.signature.Signature
import com.walletconnect.android.utils.cacao.CacaoSignerInterface
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.hexToBytes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign.getEthereumMessageHash
import org.web3j.utils.Numeric

internal class EIP191SignerTest {

    private val cacaoSigner = object : CacaoSignerInterface<Cacao.Signature> {}
    private val message = "Message"
    private val privateKey = "5103d48fdbc5e0a53b56ab97ab51e6ab2b21566a8524e9f4f4fb396689943277".hexToBytes()
    private val cacaoAsJson = """{
  "h": {
    "t": "eip4361"
  },
  "p": {
    "iss": "did:pkh:eip155:1:0xf457f233ab23f863cabc383ebb37b29d8929a17a",
    "domain": "http://10.0.2.2:8080",
    "aud": "http://10.0.2.2:8080",
    "version": "1",
    "nonce": "[B@c3772c7",
    "iat": "2023-01-17T12:15:05+01:00",
    "resources": [
      "did:key:z6MkkG9nM8ksS37sq5mgeoCn5kihLkWANcm9pza5WTkq3tWZ"
    ]
  },
  "s": {
    "t": "eip191",
    "s": "0x1b39982707c70c95f4676e7386052a07b47ecc073b3e9cf47b64b579687d3f68181d48fa9e926ad591ba6954f1a70c597d0772a800bed5fa906384fcd83bcf4f1b"
  }
}"""

    @Test
    fun signAndVerify() {
        val address = Keys.getAddress(ECKeyPair.create(privateKey))
        val signature = EIP191Signer.sign(message, privateKey)
        val result = EIP191Verifier.verify(signature, message, address)
        assertTrue(result)
    }

    @Test
    fun signHexAndVerify() {
        val address = Keys.getAddress(ECKeyPair.create(privateKey))
        val signature = EIP191Signer.signHex(Numeric.toHexString(message.toByteArray()), privateKey)
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

    @Test
    fun signHexAndVerifyNoPrefix() {
        val address = Keys.getAddress(ECKeyPair.create(privateKey))
        val signature = EIP191Signer.signHexNoPrefix(Numeric.toHexString(message.toByteArray()), privateKey)
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

    @Test
    fun keyserverSignAndVerify() {
        val pair = ECKeyPair.create(privateKey)
        println(pair.publicKey.toByteArray().bytesToHex())
        val address = Keys.getAddress(pair)
        println(address)

        val moshi: Moshi = Moshi.Builder().build()
        val jsonAdapter: JsonAdapter<Cacao> = moshi.adapter(Cacao::class.java)
        val cacao = jsonAdapter.fromJson(cacaoAsJson)
        println(cacao)
        val message = cacao!!.payload.toCAIP122Message()
        val signature = cacaoSigner.sign(message, privateKey, SignatureType.EIP191)
        println(signature)
        println("Message:")
        println("\u0019Ethereum Signed Message:\n${message.toByteArray(Charsets.UTF_8).size}$message")
        println("Hash:")
        println(Hash.sha3(getEthereumMessageHash(message.toByteArray()).bytesToHex()).removePrefix("0x"))
        val result = EIP191Verifier.verify(signature.toSignature(), message, address)
        assertTrue(result)
    }
}