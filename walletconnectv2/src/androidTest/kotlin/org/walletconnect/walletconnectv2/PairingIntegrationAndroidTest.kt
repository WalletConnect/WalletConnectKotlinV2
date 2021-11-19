package org.walletconnect.walletconnectv2

import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.WalletConnectClientData
import org.walletconnect.walletconnectv2.client.WalletConnectClientListener

class PairingIntegrationAndroidTest {
    @get:Rule
    val activityRule = WCIntegrationActivityScenarioRule()
    private val app = ApplicationProvider.getApplicationContext<IntegrationTestApplication>()

    @Test
    fun pairing() {
        activityRule.launch() {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:1420bdd67db1c9da97e976a85dcca60cbc2cc2f7566c3851fbd2fa07d2f4587e@2?controller=false&publicKey=3e21974849ebf1f95274679e7f5a3ab5fc607ae921a0dffd756ce634d9b65b5b&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)
            val listener = object : WalletConnectClientListener {
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
        activityRule.launch() {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:970b0fa8367670e0411a95b27c8bd30a13b26f7f9173fb8111d581d53f03fd45@2?controller=false&publicKey=2130cf44d346b571615284ffae0c97195ca45464b18d8ff459105d84fe3aa70e&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)
            val listener = object : WalletConnectClientListener {
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