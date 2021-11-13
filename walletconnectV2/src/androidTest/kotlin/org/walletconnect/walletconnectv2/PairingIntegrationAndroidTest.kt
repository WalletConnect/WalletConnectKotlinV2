package org.walletconnect.walletconnectv2

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.rules.activityScenarioRule
import org.junit.*
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.WalletConnectClientData
import org.walletconnect.walletconnectv2.client.WalletConnectClientListener
import org.walletconnect.walletconnectv2.util.Logger
import java.util.concurrent.CountDownLatch

class PairingIntegrationAndroidTest {
    @get:Rule
    val activityRule = wcActivityScenarioRule()
    private val app = ApplicationProvider.getApplicationContext<IntegrationTestApplication>()

    @Test
    fun pairing() {
        activityRule.launch {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:6fbac9a32042957b0791128874655ac5c2db64016c293b29e77484fe5d868d6a@2?controller=false&publicKey=23010bc280c5fadada0fd93f012e2c1aa0797bcf348b15a74d56b56d3ddec700&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)
            val listener = object: WalletConnectClientListener {
                override fun onSessionProposal(proposal: WalletConnectClientData.SessionProposal) {
                    assert(true)
                    activityRule.close()
                }

                override fun onSettledSession(session: WalletConnectClientData.SettledSession) {}

                override fun onSessionRequest(request: WalletConnectClientData.SessionRequest) {}

                override fun onSessionDelete(topic: String, reason: String) {}
            }

            WalletConnectClient.pair(pairingParams, listener)
        }
    }

    @Test
    fun approve() {
        activityRule.launch {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:970b0fa8367670e0411a95b27c8bd30a13b26f7f9173fb8111d581d53f03fd45@2?controller=false&publicKey=2130cf44d346b571615284ffae0c97195ca45464b18d8ff459105d84fe3aa70e&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)
            val listener = object: WalletConnectClientListener {
                override fun onSessionProposal(proposal: WalletConnectClientData.SessionProposal) {
                    assert(true)

                    val accounts = proposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
                    val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(proposal, accounts)

                    WalletConnectClient.approve(approveParams)
                }

                override fun onSettledSession(session: WalletConnectClientData.SettledSession) {
                    assert(true)
                    activityRule.close()
                }

                override fun onSessionRequest(request: WalletConnectClientData.SessionRequest) {}

                override fun onSessionDelete(topic: String, reason: String) {}
            }

            WalletConnectClient.pair(pairingParams, listener)
        }
    }
}