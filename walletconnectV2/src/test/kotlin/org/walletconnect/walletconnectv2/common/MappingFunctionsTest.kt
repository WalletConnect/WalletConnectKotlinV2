package org.walletconnect.walletconnectv2.common

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.clientcomm.PreSettlementPairing
import org.walletconnect.walletconnectv2.clientcomm.pairing.Pairing
import org.walletconnect.walletconnectv2.clientcomm.pairing.proposal.PairingProposer
import org.walletconnect.walletconnectv2.clientcomm.pairing.success.PairingParticipant
import org.walletconnect.walletconnectv2.clientcomm.pairing.success.PairingState
import org.walletconnect.walletconnectv2.common.network.adapters.ExpiryAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.JSONObjectAdapter
import org.walletconnect.walletconnectv2.common.network.adapters.TopicAdapter
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
        val pairingProposal = mockk<Pairing.Proposal>() {
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
        val pairingProposal = mockk<Pairing.Proposal>() {
            every { topic } returns Topic(getRandom64ByteHexString())
            every { relay } returns mockk()
            every { pairingProposer } returns PairingProposer("0x123", false)
            every { ttl } returns mockk()
        }

        val wcPairingApprove = pairingProposal.toApprove(
            randomId,
            settledTopic,
            expiry,
            PublicKey("0x123")
        )

        assertEquals(randomId, wcPairingApprove.id)
        assertEquals(pairingProposal.toPairingSuccess(settledTopic, expiry, PublicKey("0x123")), wcPairingApprove.params)
    }

    @Test
    fun `PreSettlementPairing_Approve to RelayPublish_Request`() {
        val moshi = Moshi.Builder()
            .addLast(ExpiryAdapter as JsonAdapter<Expiry>)
            .addLast(TopicAdapter as JsonAdapter<Topic>)
            .addLast(JSONObjectAdapter as JsonAdapter<JSONObject>)
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val preSettlementPairingApprove = mockk<PreSettlementPairing.Approve>() {
            every { id } returns 1
            every { jsonrpc } returns "2.0"
            every { method } returns "wc_pairingApprove"
            every { params } returns Pairing.Success(Topic(getRandom64ByteHexString()) /*settle topic*/, JSONObject(), PairingParticipant(getRandom64ByteHexString()), Expiry(100L), PairingState(null))
        }

        //TODO test failing, review
        val relayPublishRequest = preSettlementPairingApprove.toRelayPublishRequest(1, Topic(getRandom64ByteHexString()), moshi)

        assert(relayPublishRequest.params.message.isNotBlank())
        assertFalse(relayPublishRequest.params.message.contains(" "))
        assertFalse(relayPublishRequest.params.message.contains(","))
    }
}