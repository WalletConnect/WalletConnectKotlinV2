package org.walletconnect.walletconnectv2

import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.WalletConnectClientData
import org.walletconnect.walletconnectv2.client.WalletConnectClientListener
import org.walletconnect.walletconnectv2.client.WalletConnectClientListeners
import org.walletconnect.walletconnectv2.common.AppMetaData
import org.walletconnect.walletconnectv2.util.Logger
import org.walletconnect.walletconnectv2.utils.IntegrationTestApplication

class WalletConnectClientIntegrationAndroidTest {
    @get:Rule
    val activityRule = WCIntegrationActivityScenarioRule()
    private val app = ApplicationProvider.getApplicationContext<IntegrationTestApplication>()

    @Test
    fun responderApprovePairingAndGetSessionProposalTest() {
        activityRule.launch {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)
            val uri =
                "wc:1420bdd67db1c9da97e976a85dcca60cbc2cc2f7566c3851fbd2fa07d2f4587e@2?controller=false&publicKey=3e21974849ebf1f95274679e7f5a3ab5fc607ae921a0dffd756ce634d9b65b5b&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)

            val listener = object : WalletConnectClientListener {
                override fun onSessionProposal(sessionProposal: WalletConnectClientData.SessionProposal) {
                    assert(true)
                    activityRule.close()
                }

                override fun onSessionRequest(sessionRequest: WalletConnectClientData.SessionRequest) {}
                override fun onSessionDelete(topic: String, reason: String) {}
            }
            WalletConnectClient.setWalletConnectListener(listener)

            WalletConnectClient.pair(pairingParams, object : WalletConnectClientListeners.Pairing {
                override fun onSuccess(settledPairing: WalletConnectClientData.SettledPairing) {
                    assert(true)
                }

                override fun onError(error: Throwable) {
                    assert(false)
                    activityRule.close()
                }
            })
        }
    }

    @Test
    fun responderSessionApproveTest() {
        activityRule.launch {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:970b0fa8367670e0411a95b27c8bd30a13b26f7f9173fb8111d581d53f03fd45@2?controller=false&publicKey=2130cf44d346b571615284ffae0c97195ca45464b18d8ff459105d84fe3aa70e&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)
            val listener = object : WalletConnectClientListener {
                override fun onSessionProposal(sessionProposal: WalletConnectClientData.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
                    val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(sessionProposal, accounts)
                    WalletConnectClient.approve(approveParams, object : WalletConnectClientListeners.SessionApprove {
                        override fun onSuccess(settledSession: WalletConnectClientData.SettledSession) {
                            assert(true)
                            activityRule.close()
                        }

                        override fun onError(error: Throwable) {
                            assert(false)
                            activityRule.close()
                        }

                    })
                }

                override fun onSessionRequest(sessionRequest: WalletConnectClientData.SessionRequest) {}

                override fun onSessionDelete(topic: String, reason: String) {}
            }

            WalletConnectClient.setWalletConnectListener(listener)
            WalletConnectClient.pair(pairingParams, object : WalletConnectClientListeners.Pairing {
                override fun onSuccess(settledPairing: WalletConnectClientData.SettledPairing) {
                    assert(true)
                }

                override fun onError(error: Throwable) {
                    assert(false)
                    activityRule.close()
                }
            })
        }
    }

    @Test
    fun responderAcceptRequestAndSendResponseTest() {
        activityRule.launch {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:e660bf452fa59d0f48076158e5d5385c7fd60ad4a1f3598bd5b109ab173f553f@2?controller=false&publicKey=3ed1ab47eba882d6d9956075cf28e326b26940376cee2b384345a70c6afb8c29&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)


            val listener = object : WalletConnectClientListener {
                override fun onSessionProposal(sessionProposal: WalletConnectClientData.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62" }
                    val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams, object : WalletConnectClientListeners.SessionApprove {
                        override fun onSuccess(settledSession: WalletConnectClientData.SettledSession) {
                            assert(true)
                        }

                        override fun onError(error: Throwable) {
                            assert(false)
                            activityRule.close()
                        }
                    })
                }

                override fun onSessionRequest(sessionRequest: WalletConnectClientData.SessionRequest) {
                    val result = ClientTypes.ResponseParams(
                        sessionTopic = sessionRequest.topic,
                        jsonRpcResponse = WalletConnectClientData.JsonRpcResponse.JsonRpcResult(
                            sessionRequest.request.id,
                            "0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"
                        )
                    )

                    WalletConnectClient.respond(result, object : WalletConnectClientListeners.SessionPayload {
                        override fun onSuccess(sessionPayloadResponse: WalletConnectClientData.Response) {
                            assert(true)
                            activityRule.close()
                        }

                        override fun onError(error: Throwable) {
                            assert(false)
                            activityRule.close()
                        }
                    })

                }

                override fun onSessionDelete(topic: String, reason: String) {}
            }

            WalletConnectClient.setWalletConnectListener(listener)
            WalletConnectClient.pair(pairingParams, object : WalletConnectClientListeners.Pairing {
                override fun onSuccess(settledPairing: WalletConnectClientData.SettledPairing) {
                    assert(true)
                }

                override fun onError(error: Throwable) {
                    assert(false)
                    activityRule.close()
                }

            })
        }
    }

    @Test
    fun responderAcceptRequestAndSendErrorTest() {
        activityRule.launch {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:9be2970fb50c380e4c33057484d07fb21e2b8507aa666d44fbdcc021319f4ea5@2?controller=false&publicKey=249f12a7e8ab849fadb56dd3bca7975e2c132c423fdf985656ae9a06f6737c67&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)


            val listener = object : WalletConnectClientListener {
                override fun onSessionProposal(sessionProposal: WalletConnectClientData.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62" }
                    val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams, object : WalletConnectClientListeners.SessionApprove {
                        override fun onSuccess(settledSession: WalletConnectClientData.SettledSession) {
                            assert(true)
                        }

                        override fun onError(error: Throwable) {
                            assert(false)
                            activityRule.close()
                        }
                    })
                }

                override fun onSessionRequest(sessionRequest: WalletConnectClientData.SessionRequest) {
                    val result = ClientTypes.ResponseParams(
                        sessionTopic = sessionRequest.topic,
                        jsonRpcResponse = WalletConnectClientData.JsonRpcResponse.JsonRpcError(
                            sessionRequest.request.id,
                            WalletConnectClientData.JsonRpcResponse.Error(500, "Kotlin Wallet Error")
                        )
                    )

                    WalletConnectClient.respond(result, object : WalletConnectClientListeners.SessionPayload {
                        override fun onSuccess(sessionPayloadResponse: WalletConnectClientData.Response) {
                            assert(true)
                            activityRule.close()
                        }

                        override fun onError(error: Throwable) {
                            assert(false)
                            activityRule.close()
                        }
                    })

                }

                override fun onSessionDelete(topic: String, reason: String) {}
            }

            WalletConnectClient.setWalletConnectListener(listener)
            WalletConnectClient.pair(pairingParams, object : WalletConnectClientListeners.Pairing {
                override fun onSuccess(settledPairing: WalletConnectClientData.SettledPairing) {
                    assert(true)
                }

                override fun onError(error: Throwable) {
                    assert(false)
                    activityRule.close()
                }

            })
        }
    }

    @Test
    fun responderSessionUpdateTest() {
        activityRule.launch {
            val metadata = AppMetaData(
                name = "Kotlin Wallet",
                description = "Wallet description",
                url = "example.wallet",
                icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
            )
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org", metadata = metadata)
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:22e937acd12c01137205364eaf728e1d22145e00a940be897a9001016e8a2993@2?controller=false&publicKey=39ece35fea57363a1e00017d8336ecd1d9a1f28517ea041853aea2084a9ce730&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)


            val listener = object : WalletConnectClientListener {
                override fun onSessionProposal(sessionProposal: WalletConnectClientData.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62" }
                    val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams, object : WalletConnectClientListeners.SessionApprove {
                        override fun onSuccess(settledSession: WalletConnectClientData.SettledSession) {

                            val updateParams = ClientTypes.UpdateParams(
                                settledSession.topic,
                                WalletConnectClientData.SessionState(accounts = listOf("eip155:8001:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716"))
                            )

                            WalletConnectClient.update(updateParams, object : WalletConnectClientListeners.SessionUpdate {
                                override fun onSuccess(updatedSession: WalletConnectClientData.UpdatedSession) {
                                    Logger.error("Session Update Success: $updatedSession")
                                    assert(true)
                                    activityRule.close()
                                }

                                override fun onError(error: Throwable) {
                                    Logger.error("Session Update Error")

                                    assert(false)
                                    activityRule.close()
                                }

                            })
                        }

                        override fun onError(error: Throwable) {
                            assert(false)
                            activityRule.close()
                        }
                    })
                }

                override fun onSessionRequest(sessionRequest: WalletConnectClientData.SessionRequest) {}
                override fun onSessionDelete(topic: String, reason: String) {}
            }

            WalletConnectClient.setWalletConnectListener(listener)
            WalletConnectClient.pair(pairingParams, object : WalletConnectClientListeners.Pairing {
                override fun onSuccess(settledPairing: WalletConnectClientData.SettledPairing) {
                    assert(true)
                }

                override fun onError(error: Throwable) {
                    assert(false)
                    activityRule.close()
                }

            })
        }
    }
}