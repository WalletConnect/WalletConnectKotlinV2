package org.walletconnect.walletconnectv2

import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.walletconnect.walletconnectv2.client.ClientTypes
import org.walletconnect.walletconnectv2.client.WalletConnectClientData
import org.walletconnect.walletconnectv2.client.WalletConnectClientListener
import org.walletconnect.walletconnectv2.client.WalletConnectClientListeners
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
                override fun onSessionDelete(deletedSession: WalletConnectClientData.DeletedSession) {}

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
                "wc:e198451f91ee660d6b7aa109a5102d697948257dd065eff819f4dc8a83c9866a@2?controller=false&publicKey=3bc0fb5d92c551ef31c4e9608d67162db226bc125b6dcd87f8b7425644f1f51f&relay=%7B%22protocol%22%3A%22waku%22%7D"
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
                override fun onSessionDelete(deletedSession: WalletConnectClientData.DeletedSession) {}
            }

            WalletConnectClient.setWalletConnectListener(listener)
            WalletConnectClient.pair(pairingParams, object : WalletConnectClientListeners.Pairing {
                override fun onSuccess(settledPairing: WalletConnectClientData.SettledPairing) {
                }

                override fun onError(error: Throwable) {
                    assert(false)
                    activityRule.close()
                }

            })
        }
    }

    @Test
    fun responderUpgradeSessionPermissionsTest() {
        activityRule.launch {
            val initParams = ClientTypes.InitialParams(application = app, hostName = "relay.walletconnect.org")
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:baeab320d7dc7c388b8700bb96c1a7e3d7398e2e104979bdcbbba3844afb2034@2?controller=false&publicKey=9178ee116da13f06dfb85c7fb4af425468359dec4f420a465ad8527cfe0c2528&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = ClientTypes.PairParams(uri)

            val listener = object : WalletConnectClientListener {
                override fun onSessionProposal(sessionProposal: WalletConnectClientData.SessionProposal) {
                    assert(true)

                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
                    val approveParams: ClientTypes.ApproveParams = ClientTypes.ApproveParams(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams, object : WalletConnectClientListeners.SessionApprove {
                        override fun onSuccess(settledSession: WalletConnectClientData.SettledSession) {
                            val permissions =
                                WalletConnectClientData.SessionPermissions(
                                    blockchain = WalletConnectClientData.Blockchain(chains = listOf("eip155:80001")),
                                    jsonRpc = WalletConnectClientData.Jsonrpc(listOf("eth_sign"))
                                )
                            val upgradeParams = ClientTypes.UpgradeParams(settledSession.topic, permissions)
                            WalletConnectClient.upgrade(upgradeParams, object : WalletConnectClientListeners.SessionUpgrade {
                                override fun onSuccess(upgradedSession: WalletConnectClientData.UpgradedSession) {
                                    assert(true)
                                    activityRule.close()
                                }

                                override fun onError(error: Throwable) {
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
                override fun onSessionDelete(deletedSession: WalletConnectClientData.DeletedSession) {}

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

                override fun onSessionDelete(deletedSession: WalletConnectClientData.DeletedSession) {}
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
                "wc:e36fd9bf661d036ceec2fd9e0c8d83423cbef918b3b89651b97c20b68953e53e@2?controller=false&publicKey=cbdde97fda16e407b9afceb0dd1bc923ecd52ef9a66948731e644748fe13c179&relay=%7B%22protocol%22%3A%22waku%22%7D"
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

                override fun onSessionDelete(deletedSession: WalletConnectClientData.DeletedSession) {}
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