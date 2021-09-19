package org.walletconnect.walletconnectv2.common

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.clientsync.PreSettlementPairing
import org.walletconnect.walletconnectv2.getRandom64ByteString
import org.walletconnect.walletconnectv2.outofband.pairing.Pairing
import org.walletconnect.walletconnectv2.outofband.pairing.proposal.PairingProposer
import org.walletconnect.walletconnectv2.outofband.pairing.success.PairingParticipant
import org.walletconnect.walletconnectv2.outofband.pairing.success.PairingState
import kotlin.test.assertEquals

internal class MappingFunctionsTest {

    @Test
    fun `A proper URI mapped to a PairingProposal`() {
        val testUri = "wc:0b1a3d6c0336662dddb6278ee0aa25380569b79e7e86cfe39fb20b4b189096a0@2?controller=false&publicKey=66db1bd5fad65392d1d5a4856d0d549d2fca9194327138b41c289b961d147860&relay=%7B%22protocol%22%3A%22waku%22%7D"

        val pairingProposal = testUri.toPairProposal()

        assertNotNull(pairingProposal)
        assert(pairingProposal.topic.topicValue.isNotBlank())
        assertFalse(pairingProposal.relay.isEmpty)
        assert(pairingProposal.pairingProposer.publicKey.isNotBlank())
        assert(pairingProposal.ttl.seconds > 0)
    }

    @Test
    fun `PairingProposal mapped to PairingSuccess`() {
        val pairingProposal = mockk<Pairing.Proposal>() {
            every { topic } returns Topic(getRandom64ByteString())
            every { relay } returns mockk()
            every { pairingProposer } returns PairingProposer(getRandom64ByteString(), false)
            every { ttl } returns mockk()
        }

        val pairingSuccess = pairingProposal.toPairingSuccess()

        assertEquals(pairingProposal.topic, pairingSuccess.topic)
        assertEquals(pairingProposal.relay, pairingSuccess.relay)
        assertEquals(pairingProposal.pairingProposer.publicKey, pairingSuccess.responder.publicKey)
        assert((pairingSuccess.expiry.seconds - pairingProposal.ttl.seconds) > 0)
        assert(pairingSuccess.state == PairingState(null))
    }

    @Test
    fun `PairingSuccess mapped to PreSettlementPairing_Approve`() {
        val randomId = 1
        val pairingProposal = mockk<Pairing.Proposal>() {
            every { topic } returns Topic(getRandom64ByteString())
            every { relay } returns mockk()
            every { pairingProposer } returns PairingProposer(getRandom64ByteString(), false)
            every { ttl } returns mockk()
        }

        val wcPairingApprove = pairingProposal.toApprove(randomId)

        assertEquals(randomId, wcPairingApprove.id)
        assertEquals(pairingProposal.toPairingSuccess(), wcPairingApprove.params)
    }

    @Test
    fun `PreSettlementPairing_Approve to RelayPublish_Request`() {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val preSettlementPairingApprove = mockk<PreSettlementPairing.Approve>() {
            every { id } returns 1
            every { jsonrpc } returns "2.0"
            every { method } returns "wc_pairingApprove"
            every { params } returns Pairing.Success(Topic(getRandom64ByteString()) /*settle topic*/, JSONObject(), PairingParticipant(getRandom64ByteString()), Expiry(100L), PairingState(null))
        }

        val relayPublishRequest = preSettlementPairingApprove.toRelayPublishRequest(1, Topic(getRandom64ByteString()), moshi)

        assert(relayPublishRequest.params.message.isNotBlank())
        assertFalse(relayPublishRequest.params.message.contains(" "))
        assertFalse(relayPublishRequest.params.message.contains(","))
    }
}