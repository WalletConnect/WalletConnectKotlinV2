package org.walletconnect.walletconnectv2

import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.walletconnect.walletconnectv2.client.ClientTypes

class PairingIntegrationAndroidTest {
    @get:Rule
    val activityScenarioRule = wcActivityScenarioRule()

    private val app = ApplicationProvider.getApplicationContext<IntegrationTestApplication>()

    @Test
    fun pairing() {
        activityScenarioRule.launch {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:619591c3794efd14e6d0a6dd123bde50fb89259230fe45c9f460a692aa39e003@2?controller=false&publicKey=e9b4dc1c620ae5042629675f726d3edf83d0588f83128bea85b9f6170a0a276b&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)

            WalletConnectClient.pair(pairingParams) {
                assert(true)
                activityScenarioRule.close()
            }
        }
    }

    @Test
    fun approve() {
        activityScenarioRule.launch {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:4b8fcbd3e675829878001823d04f12546ee05735f60eb164efb4366fc8dce097@2?controller=false&publicKey=2043224e78135de033df55579b8e65fa2e37d55564912d09e9aaaa043f28ef50&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)

            WalletConnectClient.pair(pairingParams) { sessionProposal ->
                assert(true)

                val proposerPublicKey: String = sessionProposal.proposerPublicKey
                val proposalTtl: Long = sessionProposal.ttl
                val proposalTopic: String = sessionProposal.topic
                val accounts = sessionProposal.chains.map { chainId ->
                    "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716"
                }
                val approveParams: ClientTypes.ApproveParams =
                    ClientTypes.ApproveParams(accounts, proposerPublicKey, proposalTtl, proposalTopic)

                WalletConnectClient.approve(approveParams) {
                    assert(true)
                    activityScenarioRule.close()
                }
            }
        }
    }
}