package org.walletconnect.walletconnectv2.relay

import io.mockk.coEvery
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.clientsync.PreSettlementPairing
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.outofband.pairing.Pairing
import org.walletconnect.walletconnectv2.outofband.pairing.success.PairingParticipant
import org.walletconnect.walletconnectv2.outofband.pairing.success.PairingState
import org.walletconnect.walletconnectv2.relay.data.model.Relay
import org.walletconnect.walletconnectv2.util.CoroutineTestRule
import org.walletconnect.walletconnectv2.util.getRandom64ByteHexString
import org.walletconnect.walletconnectv2.util.runTest
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class WakuRelayRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val sut = spyk(WakuRelayRepository.initRemote(hostName = "127.0.0.1"))

    @Test
    fun `Publish a pairing request, expect a successful response`() {
        // Arrange
        val topic = Topic(getRandom64ByteHexString())
        val settledTopic = Topic(getRandom64ByteHexString())
        val preSettlementPairing = PreSettlementPairing.Approve(
            id = 1,
            params = Pairing.Success(
                settledTopic = settledTopic,
                relay = JSONObject(),
                responder = PairingParticipant(getRandom64ByteHexString()),
                expiry = Expiry(1),
                state = PairingState()
            )
        )
        coEvery { sut.publishResponse } returns flowOf(Relay.Publish.Response(id = preSettlementPairing.id, result = true))

        // Act
        sut.publish(topic, preSettlementPairing)

        // Assert
        coroutineTestRule.runTest {
            sut.publishResponse.collect {
                assertEquals(preSettlementPairing.id, it.id)
                assertEquals(true, it.result)
            }
        }
    }
}