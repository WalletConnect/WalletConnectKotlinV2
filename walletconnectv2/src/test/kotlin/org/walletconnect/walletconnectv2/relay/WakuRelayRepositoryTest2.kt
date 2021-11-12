package org.walletconnect.walletconnectv2.relay

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.walletconnect.walletconnectv2.clientsync.pairing.Pairing
import org.walletconnect.walletconnectv2.clientsync.pairing.before.PreSettlementPairing
import org.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingParticipant
import org.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingState
import org.walletconnect.walletconnectv2.common.Expiry
import org.walletconnect.walletconnectv2.common.Topic
import org.walletconnect.walletconnectv2.relay.data.model.Relay
import org.walletconnect.walletconnectv2.util.CoroutineTestRule
import org.walletconnect.walletconnectv2.util.getRandom64ByteHexString
import org.walletconnect.walletconnectv2.util.runTest
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class WakuRelayRepositoryTest2 {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val relayFactory = WakuRelayRepository.RelayFactory(useTls = true, hostName = "127.0.0.1", apiKey = "", application = app)
    private val sut = spyk(WakuRelayRepository.initRemote(relayFactory))

    @Test
    fun `Publish a pairing request, expect a successful acknowledgement`() {
        // Arrange
        val topic = Topic(getRandom64ByteHexString())
        val settledTopic = Topic(getRandom64ByteHexString())
        val preSettlementPairing = PreSettlementPairing.Approve(
            id = 1L,
            params = Pairing.Success(
                settledTopic = settledTopic,
                relay = JSONObject(),
                responder = PairingParticipant(getRandom64ByteHexString()),
                expiry = Expiry(1),
                state = PairingState()
            )
        )
        coEvery { sut.publishAcknowledgement } returns flowOf(Relay.Publish.Acknowledgement(id = preSettlementPairing.id, result = true))

        // Act
        sut.publishPairingApproval(topic, preSettlementPairing)

        // Assert
        coroutineTestRule.runTest {
            sut.publishAcknowledgement.collect {
                assertEquals(preSettlementPairing.id, it.id)
                assertEquals(true, it.result)
            }
        }
    }
}