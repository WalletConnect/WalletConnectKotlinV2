package com.walletconnect.walletconnectv2.relay

import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class WakuRelayRepositoryTest {

//    @get:Rule
//    val coroutineTestRule = CoroutineTestRule()
//
//    private val relayFactory = WakuNetworkRepository.WakuNetworkFactory(true, "127.0.0.1", "", Application())
//    private val sut = spyk(WakuNetworkRepository.init(relayFactory))
//
//    @Test
//    fun `Publish a pairing request, expect a successful acknowledgement`() {
//        // Arrange
//        val topic = TopicVO(getRandom64ByteHexString())
//        val settledTopic = TopicVO(getRandom64ByteHexString())
//        val preSettlementPairing = PreSettlementPairingVO.Approve(
//            id = 1L,
//            params = PairingParamsVO.Success(
//                settledTopic = settledTopic,
//                relay = JSONObject(),
//                responder = PairingParticipantVO(getRandom64ByteHexString()),
//                expiry = ExpiryVO(1),
//                state = com.walletconnect.walletconnectv2.core.model.vo.clientsync.pairing.before.success.PairingStateVO()
//            )
//        )
//        coEvery { sut.observePublishAcknowledgement } returns flowOf(
//            RelayDTO.Publish.Acknowledgement(
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
//    }
}