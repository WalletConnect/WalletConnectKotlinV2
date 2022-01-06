package com.walletconnect.walletconnectv2.relay

import android.app.Application
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject
import org.junit.Rule
import org.junit.jupiter.api.Test
import com.walletconnect.walletconnectv2.clientsync.pairing.Pairing
import com.walletconnect.walletconnectv2.clientsync.pairing.before.PreSettlementPairing
import com.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingParticipant
import com.walletconnect.walletconnectv2.clientsync.pairing.before.success.PairingState
import com.walletconnect.walletconnectv2.common.Expiry
import com.walletconnect.walletconnectv2.common.Topic
import com.walletconnect.walletconnectv2.relay.waku.Relay
import com.walletconnect.walletconnectv2.relay.waku.WakuNetworkRepository
import com.walletconnect.walletconnectv2.util.CoroutineTestRule
import com.walletconnect.walletconnectv2.util.getRandom64ByteHexString
import com.walletconnect.walletconnectv2.util.runTest
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class WakuRelayRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

//    private val relayFactory = WakuNetworkRepository.WakuNetworkFactory(true, "127.0.0.1", "", Application())
//    private val sut = spyk(WakuNetworkRepository.init(relayFactory))

    @Test
    fun `Publish a pairing request, expect a successful acknowledgement`() {
        // Arrange
//        val topic = Topic(getRandom64ByteHexString())
//        val settledTopic = Topic(getRandom64ByteHexString())
//        val preSettlementPairing = PreSettlementPairing.Approve(
//            id = 1L,
//            params = Pairing.Success(
//                settledTopic = settledTopic,
//                relay = JSONObject(),
//                responder = PairingParticipant(getRandom64ByteHexString()),
//                expiry = Expiry(1),
//                state = PairingState()
//            )
//        )
//        coEvery { sut.observePublishAcknowledgement } returns flowOf(
//            Relay.Publish.Acknowledgement(
//                id = preSettlementPairing.id,
//                result = true
//            )
//        )
//
//        // Act
//        sut.publish(topic, preSettlementPairing.toString())
//
//        // Assert
//        coroutineTestRule.runTest {
//            sut.observePublishAcknowledgement.collect {
//                assertEquals(preSettlementPairing.id, it.id)
//                assertEquals(true, it.result)
//            }
//        }
    }
}