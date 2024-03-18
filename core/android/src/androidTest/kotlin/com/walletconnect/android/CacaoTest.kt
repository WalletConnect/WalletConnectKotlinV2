package com.walletconnect.android

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.cacao.Cacao
import com.walletconnect.android.internal.common.signing.cacao.CacaoType
import com.walletconnect.android.internal.common.signing.cacao.CacaoVerifier
import com.walletconnect.android.internal.common.signing.cacao.toCAIP222Message
import com.walletconnect.android.utils.cacao.CacaoSignerInterface
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.util.hexToBytes
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.web3j.utils.Numeric

internal class CacaoTest {
    private val cacaoVerifier = CacaoVerifier(ProjectId(BuildConfig.PROJECT_ID))
    private val cacaoSigner = object : CacaoSignerInterface<Cacao.Signature> {}
    private val iss = "did:pkh:eip155:1:0x15bca56b6e2728aec2532df9d436bd1600e86688"
    private val chainName = "Ethereum"
    private val payload = Cacao.Payload(
        iss = iss,
        domain = "service.invalid",
        aud = "https://service.invalid/login",
        version = "1",
        nonce = "32891756",
        iat = "2021-09-30T16:25:24Z",
        nbf = null,
        exp = null,
        statement = "I accept the ServiceOrg Terms of Service: https://service.invalid/tos",
        requestId = null,
        resources = listOf(
            "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/",
            "https://example.com/my-web2-claim.json",
            "urn:recap:eyJhdHQiOnsiZWlwMTU1Ijp7InJlcXVlc3QvcGVyc29uYWxfc2lnbiI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XSwicmVxdWVzdC9ldGhfc2lnblR5cGVkRGF0YV92NCI6W3siY2hhaW5zIjpbImVpcDE1NToxIl19XX19fQ=="
        )
    )

    private val privateKey = "305c6cde3846927892cd32762f6120539f3ec74c9e3a16b9b798b1e85351ae2a".hexToBytes()

    @Test
    fun signAndVerifyWithEIP191Test() {
        val message = payload.toCAIP222Message(chainName)
        val signature: Cacao.Signature = cacaoSigner.sign(message, privateKey, SignatureType.EIP191)
        val cacao = Cacao(CacaoType.CAIP222.toHeader(), payload, signature)
        val result: Boolean = cacaoVerifier.verify(cacao)
        Assert.assertTrue(result)
    }

    @Test
    fun signHexAndVerifyWithEIP191Test() {
        val message = payload.toCAIP222Message(chainName)
        val signature: Cacao.Signature = cacaoSigner.sign(Numeric.toHexString(message.toByteArray()), privateKey, SignatureType.EIP191)
        val cacao = Cacao(CacaoType.CAIP222.toHeader(), payload, signature)
        val result: Boolean = cacaoVerifier.verify(cacao)
        assert(result)
    }

    @Test
    @Ignore("Test failing in pipeline")
    fun verifyEIP1271Success() {
        val iss = "did:pkh:eip155:1:0x6DF3d14554742D67068BB7294C80107a3c655A56"
        val payload = Cacao.Payload(
            iss = iss,
            domain = "etherscan.io",
            aud = "https://etherscan.io/verifiedSignatures#",
            version = "1",
            nonce = "DTYxeNr95Ne7Sape5",
            iat = "2024-02-05T13:09:08.427Z",
            nbf = null,
            exp = null,
            statement = "Sign message to verify ownership of the address 0x6DF3d14554742D67068BB7294C80107a3c655A56 on etherscan.io",
            requestId = null,
            resources = null
        )

        val signatureString = "0xb518b65724f224f8b12dedeeb06f8b278eb7d3b42524959bed5d0dfa49801bd776c7ee05de396eadc38ee693c917a04d93b20981d68c4a950cbc42ea7f4264bc1c"
        val signature: Cacao.Signature = Cacao.Signature(SignatureType.EIP1271.header, signatureString, payload.toCAIP222Message())
        val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, signature)
        val result: Boolean = cacaoVerifier.verify(cacao)
        Assert.assertTrue(result)
    }


    @Test
    fun verifyEIP1271Failure() {
        val iss = "did:pkh:eip155:1:0x2faf83c542b68f1b4cdc0e770e8cb9f567b08f71"
        val payload = Cacao.Payload(
            iss = iss,
            domain = "localhost",
            aud = "http://localhost:3000/",
            version = "1",
            nonce = "1665443015700",
            iat = "2022-10-10T23:03:35.700Z",
            nbf = null,
            exp = "2022-10-11T23:03:35.700Z",
            statement = null,
            requestId = null,
            resources = null
        )

        val signatureString = "0xdeaddeaddead4095116db01baaf276361efd3a73c28cf8cc28dabefa945b8d536011289ac0a3b048600c1e692ff173ca944246cf7ceb319ac2262d27b395c82b1c"
        val signature: Cacao.Signature = Cacao.Signature(SignatureType.EIP1271.header, signatureString, payload.toCAIP222Message())
        val cacao = Cacao(CacaoType.EIP4361.toHeader(), payload, signature)
        val result: Boolean = cacaoVerifier.verify(cacao)
        Assert.assertFalse(result)
    }
}