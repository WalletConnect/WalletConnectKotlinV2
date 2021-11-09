package org.walletconnect.walletconnectv2

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.rules.activityScenarioRule
import org.junit.*
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.util.Logger
import java.util.concurrent.CountDownLatch

class PairingIntegrationAndroidTest {
    @get:Rule
    val activityRule = activityScenarioRule<IntegrationTestActivity>()
    private val app = ApplicationProvider.getApplicationContext<IntegrationTestApplication>()

    @After
    fun tearDown() {
        activityRule.scenario.close()
    }

    @Test
    fun pairing() {
        runWCClientTest { latch ->
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:91c2bd9849642c5d0d2c4ff7aeca11d072761789cd8ef5d5fedc224d1ecc6698@2?controller=false&publicKey=6fca13196ca58d630cb66396be63b29e8d284218a0feef505cf3cb0061ff6105&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)

            WalletConnectClient.pair(pairingParams) {
                assert(true)
                latch.countDown()
            }
        }
    }

    @Test
    fun approve() {
        runWCClientTest { latch ->
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
                    latch.countDown()
                }
            }
        }
    }

    private fun runWCClientTest(block: (CountDownLatch) -> Unit) {
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        assert(activityRule.scenario.state.isAtLeast(Lifecycle.State.RESUMED))

        activityRule.scenario.onActivity {
            val latch = CountDownLatch(1)

            block(latch)

            try {
                latch.await()
            } catch (exception: InterruptedException) {
                Assert.fail()
            }
        }
    }
}