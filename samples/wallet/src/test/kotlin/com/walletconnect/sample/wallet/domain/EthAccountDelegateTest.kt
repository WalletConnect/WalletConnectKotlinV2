package com.walletconnect.sample.wallet.domain

import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.internal.common.model.ProjectId
import com.walletconnect.android.internal.common.signing.message.MessageSignatureVerifier
import com.walletconnect.android.utils.cacao.sign
import com.walletconnect.util.hexToBytes
import com.walletconnect.web3.inbox.cacao.CacaoSigner
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class EthAccountDelegateTest {

    @Test
    fun `can sign messages with generated account`() {
        with(EthAccountDelegate) {
            val (_, privateKey, address) = generateKeys()
            val message = "dummy message"
            val signatureType = SignatureType.EIP191
            assertEquals(SignatureType.EIP191, signatureType) // Will fail with other types i.e SignatureType.EIP1271 requires projectId

            val signature = CacaoSigner.sign(message, privateKey.hexToBytes(), signatureType)
            val isValid = MessageSignatureVerifier(ProjectId("dummy projectid")).verify(signature.s, message, address, signatureType)

            assertTrue(isValid)
        }
    }

    @Test
    fun `cannot verify signatures with another generated account`() {
        with(EthAccountDelegate) {
            val (_, privateKey, _) = generateKeys()
            val message = "dummy message"
            val signatureType = SignatureType.EIP191
            assertEquals(SignatureType.EIP191, signatureType) // Will fail with other types i.e SignatureType.EIP1271 requires projectId

            val signature = CacaoSigner.sign(message, privateKey.hexToBytes(), signatureType)
            val address = generateKeys().third
            val isValid = MessageSignatureVerifier(ProjectId("dummy projectid")).verify(signature.s, message, address, signatureType)

            assertFalse(isValid)
        }
    }
}