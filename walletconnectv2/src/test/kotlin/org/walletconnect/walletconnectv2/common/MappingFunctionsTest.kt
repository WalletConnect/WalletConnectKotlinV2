package org.walletconnect.walletconnectv2.common

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.clientsync.pairing.Pairing
import org.walletconnect.walletconnectv2.clientsync.pairing.before.proposal.PairingProposer
import org.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingState
import org.walletconnect.walletconnectv2.crypto.data.PublicKey
import org.walletconnect.walletconnectv2.util.getRandom64ByteHexString
import kotlin.test.assertEquals

internal class MappingFunctionsTest {

    @Test
    fun `A proper URI mapped to a PairingProposal`() {
        val testUri =
            "wc:0b1a3d6c0336662dddb6278ee0aa25380569b79e7e86cfe39fb20b4b189096a0@2?controller=false&publicKey=66db1bd5fad65392d1d5a4856d0d549d2fca9194327138b41c289b961d147860&relay=%7B%22protocol%22%3A%22waku%22%7D"

        val pairingProposal = testUri.toPairProposal()

        assertNotNull(pairingProposal)
        assert(pairingProposal.topic.topicValue.isNotBlank())
        assert(pairingProposal.pairingProposer.publicKey.isNotBlank())
        assert(pairingProposal.ttl.seconds > 0)
    }

    @Test
    fun `PairingProposal mapped to PairingSuccess`() {
        val pairingProposal = mockk<Pairing.Proposal> {
            every { topic } returns Topic("0x111")
            every { relay } returns mockk()
            every { pairingProposer } returns PairingProposer("0x123", false)
            every { ttl } returns Ttl(2L)
        }

        val pairingSuccess = pairingProposal.toPairingSuccess(
            Topic("0x111"),
            Expiry(10L),
            PublicKey("0x123")
        )

        assertEquals(pairingProposal.topic, pairingSuccess.settledTopic)
        assertEquals(pairingProposal.relay, pairingSuccess.relay)
        assertEquals(pairingProposal.pairingProposer.publicKey, pairingSuccess.responder.publicKey)
        assert((pairingSuccess.expiry.seconds - pairingProposal.ttl.seconds) > 0)
        assert(pairingSuccess.state == PairingState(null))
    }

    @Test
    fun `PairingSuccess mapped to PreSettlementPairing_Approve`() {
        val randomId = 1L
        val settledTopic = Topic(getRandom64ByteHexString())
        val expiry = Expiry(1)
        val pairingProposal = mockk<Pairing.Proposal> {
            every { topic } returns Topic(getRandom64ByteHexString())
            every { relay } returns mockk()
            every { pairingProposer } returns PairingProposer("0x123", false)
            every { ttl } returns mockk()
        }

        val wcPairingApprove = pairingProposal.toApprove(randomId, settledTopic, expiry, PublicKey("0x123"))
        assertEquals(randomId, wcPairingApprove.id)
        assertEquals(pairingProposal.toPairingSuccess(settledTopic, expiry, PublicKey("0x123")), wcPairingApprove.params)
    }
}