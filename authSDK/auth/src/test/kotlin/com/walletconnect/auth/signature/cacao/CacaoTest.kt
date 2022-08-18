package com.walletconnect.auth.signature.cacao

import com.walletconnect.auth.client.Auth
import com.walletconnect.auth.signature.SignatureType
import com.walletconnect.auth.signature.toFormattedMessage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.web3j.crypto.ECKeyPair
import org.web3j.utils.Numeric

internal class CacaoTest {
    private val iss = "did:pkh:eip155:1:0x15bca56b6e2728aec2532df9d436bd1600e86688"
    private val chainName = "Ethereum"

    private val payload = Auth.Model.Cacao.Payload(
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
        resources = listOf("ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/", "https://example.com/my-web2-claim.json")
    )

    private val privateKey = "305c6cde3846927892cd32762f6120539f3ec74c9e3a16b9b798b1e85351ae2a"
    private val keyPair: ECKeyPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey))

    @Test
    fun signAndVerifyTest() {
        val signature = CacaoSigner.sign(payload.toFormattedMessage(chainName), keyPair, SignatureType.EIP191)
        val cacao = Auth.Model.Cacao(Auth.Model.Cacao.Header(SignatureType.EIP191.header), payload, signature)
        val result = CacaoVerifier.verify(cacao)
        Assertions.assertTrue(result)
    }
}