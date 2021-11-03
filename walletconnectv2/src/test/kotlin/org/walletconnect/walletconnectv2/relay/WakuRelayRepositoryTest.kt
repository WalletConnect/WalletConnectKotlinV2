package org.walletconnect.walletconnectv2.relay

import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class WakuRelayRepositoryTest {
// TODO: WakuRelayRepository now needs application. Need to either add Robolectric or move to androidTest

//    @get:Rule
//    val coroutineTestRule = CoroutineTestRule()
//
//    private val sut = spyk(WakuRelayRepository.initRemote(hostName = "127.0.0.1", apiKey = ""))
//
//    @Test
//    fun `Publish a pairing request, expect a successful acknowledgement`() {
//        // Arrange
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
//        coEvery { sut.publishAcknowledgement } returns flowOf(Relay.Publish.Acknowledgement(id = preSettlementPairing.id, result = true))
//
//        // Act
//        sut.publishPairingApproval(topic, preSettlementPairing)
//
//        // Assert
//        coroutineTestRule.runTest {
//            sut.publishAcknowledgement.collect {
//                assertEquals(preSettlementPairing.id, it.id)
//                assertEquals(true, it.result)
//            }
//        }
//    }
}