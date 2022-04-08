package com.walletconnect.walletconnectv2.wallet

import androidx.test.core.app.ApplicationProvider
import com.walletconnect.walletconnectv2.WCIntegrationActivityScenarioRule
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import com.walletconnect.walletconnectv2.util.Logger
import com.walletconnect.walletconnectv2.utils.IntegrationTestApplication
import org.junit.Rule
import org.junit.jupiter.api.Test

class WalletConnectClientIntegrationAndroidTest {
    @get:Rule
    val activityRule = WCIntegrationActivityScenarioRule()
    private val app = ApplicationProvider.getApplicationContext<IntegrationTestApplication>()

    private val metadata = WalletConnect.Model.AppMetaData(
        name = "Kotlin Wallet",
        description = "Wallet description",
        url = "example.wallet",
        icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")
    )

    @Test
    fun responderApprovePairingAndGetSessionProposalTest() {
        activityRule.launch {
            val initParams = WalletConnect.Params.Init(
                application = app,
                useTls = true,
                hostName = "relay.walletconnect.org",
                projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                metadata = metadata
            )
            WalletConnectClient.initialize(initParams)
            val uri =
                "wc:4f47abd615d5b56941989e120f108c2f338801ce16ee902237654b8c1970e8a2@2?controller=false&publicKey=2d573da1d2b8dbe3dcdb6ce7de47ce44b18fb8ec5ddc9d3f412ab4a718fff93c&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = WalletConnect.Params.Pair(uri)

            val delegate = object : WalletConnectClient.WalletDelegate {
                override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                    assert(true)
                    activityRule.close()
                }

                override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {}
                override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {}
                override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {}
                override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
                    Logger.log("Pairing settle response: $response")
                }

                override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
                    Logger.log("Session settle response: $response")
                }

                override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {}
                override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {}
            }

            WalletConnectClient.setWalletDelegate(delegate)
            WalletConnectClient.pair(pairingParams)
        }
    }

    @Test
    fun responderSessionApproveTest() {
        activityRule.launch {
            val initParams = WalletConnect.Params.Init(
                application = app,
                useTls = true,
                hostName = "relay.walletconnect.org",
                projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                metadata = metadata
            )
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:76a4fd7ab4015aa22ad77bfa0cd0bc563047fd3d92ad5285db71e80cea68f9ca@2?controller=false&publicKey=7a6875ec8512a8d77be1ddd23797a08d078627c0f85887bf0452be97b7390e34&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = WalletConnect.Params.Pair(uri)
            val listener = object : WalletConnectClient.WalletDelegate {
                override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
                    val approveParams: WalletConnect.Params.Approve = WalletConnect.Params.Approve(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams)
                }

                override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {}
                override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {}
                override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {}
                override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
                    Logger.log("Pairing settle response: $response")
                }

                override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
                    Logger.log("Session settle response: $response")
                    assert(true)
                    activityRule.close()
                }

                override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {}
                override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {}
            }

            WalletConnectClient.setWalletDelegate(listener)
            WalletConnectClient.pair(pairingParams)
        }
    }

    @Test
    fun responderUpgradeSessionPermissionsTest() {
        activityRule.launch {
            val initParams = WalletConnect.Params.Init(
                application = app,
                useTls = true,
                hostName = "relay.walletconnect.org",
                projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                metadata = metadata
            )
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:a26b323e1ad907d7c68264dbf9d5ccbd2077adae3195d23d7b052116ffcd4736@2?controller=false&publicKey=b9577a879cb2fea465ff945e409790b5996cc21dab10db48ceda23cfa3baab37&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = WalletConnect.Params.Pair(uri)

            val listener = object : WalletConnectClient.WalletDelegate {
                override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                    assert(true)

                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716" }
                    val approveParams: WalletConnect.Params.Approve = WalletConnect.Params.Approve(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams)
                }

                override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {}
                override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {}
                override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {}
                override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
                    Logger.log("Pairing settle response: $response")
                }

                override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
                    Logger.log("Session settle response: $response")

                    if (response is WalletConnect.Model.SettledSessionResponse.Result) {
                        val permissions = WalletConnect.Model.SessionPermissions(
                            blockchain = WalletConnect.Model.Blockchain(chains = listOf("eip155:80001")),
                            jsonRpc = WalletConnect.Model.JsonRpc(listOf("eth_sign"))
                        )
                        val upgradeParams = WalletConnect.Params.Upgrade(response.session.topic, permissions)

                        WalletConnectClient.upgrade(upgradeParams)
                    }
                }

                override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {
                    Logger.log("Session upgrade response: $response")
                    if (response is WalletConnect.Model.SessionUpgradeResponse.Result) {
                        assert(true)
                        activityRule.close()
                    }
                }

                override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {}

            }

            WalletConnectClient.setWalletDelegate(listener)
            WalletConnectClient.pair(pairingParams)
        }
    }

    @Test
    fun responderAcceptRequestAndSendResponseTest() {
        activityRule.launch {
            val initParams = WalletConnect.Params.Init(
                application = app,
                useTls = true,
                hostName = "relay.walletconnect.org",
                projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                metadata = metadata
            )
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:a436606363ab68232f14d46899397d2d765488a1d5b599922a5e11f3826b44eb@2?controller=false&publicKey=6868953b0b4fdbf203902dd2ea2c982a106c5656879b18df815343fe5e609a6d&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = WalletConnect.Params.Pair(uri)

            val listener = object : WalletConnectClient.WalletDelegate {

                override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62" }
                    val approveParams: WalletConnect.Params.Approve = WalletConnect.Params.Approve(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams)
                }

                override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
                    val result = WalletConnect.Params.Response(
                        sessionTopic = sessionRequest.topic,
                        jsonRpcResponse = WalletConnect.Model.JsonRpcResponse.JsonRpcResult(
                            sessionRequest.request.id,
                            "0xa3f20717a250c2b0b729b7e5becbff67fdaef7e0699da4de7ca5895b02a170a12d887fd3b17bfdce3481f10bea41f45ba9f709d39ce8325427b57afcfc994cee1b"
                        )
                    )

                    WalletConnectClient.respond(result)

                }

                override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {}
                override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {}
                override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
                    Logger.log("Pairing settle response: $response")
                }

                override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
                    Logger.log("Session settle response: $response")
                }

                override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {}
                override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {}
            }

            WalletConnectClient.setWalletDelegate(listener)
            WalletConnectClient.pair(pairingParams)
        }
    }

    @Test
    fun responderAcceptRequestAndSendErrorTest() {
        activityRule.launch {
            val initParams = WalletConnect.Params.Init(
                application = app,
                useTls = true,
                hostName = "relay.walletconnect.org",
                projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                metadata = metadata
            )
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:5435739a2365dd46bbbb2543abbad1964bb702f622428f63d0d4257ddd7df7b7@2?controller=false&publicKey=76464aa17766b58a335e8ee6a96d8be7e1bbfcd81307c57e81b0b6cd54639765&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = WalletConnect.Params.Pair(uri)


            val listener = object : WalletConnectClient.WalletDelegate {
                override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62" }
                    val approveParams: WalletConnect.Params.Approve = WalletConnect.Params.Approve(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams)
                }

                override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
                    val result = WalletConnect.Params.Response(
                        sessionTopic = sessionRequest.topic,
                        jsonRpcResponse = WalletConnect.Model.JsonRpcResponse.JsonRpcError(
                            id = sessionRequest.request.id,
                            code = 500,
                            message = "Kotlin Wallet Error"
                        )
                    )

                    WalletConnectClient.respond(result)
                }

                override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {}
                override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {}
                override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
                    Logger.log("Pairing settle response: $response")
                }

                override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
                    Logger.log("Session settle response: $response")
                }

                override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {}
                override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {}
            }

            WalletConnectClient.setWalletDelegate(listener)
            WalletConnectClient.pair(pairingParams)
        }
    }

    @Test
    fun responderSessionUpdateTest() {
        activityRule.launch {
            val initParams = WalletConnect.Params.Init(
                application = app,
                useTls = true,
                hostName = "relay.walletconnect.org",
                projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                metadata = metadata
            )
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:7518ca65d85b3084d3b5f5fb223a7cd902c8bb5faca80fbe3e4f74f936eecd20@2?controller=false&publicKey=55fa723c020e8b3d3f7cc01c0d7f7eaf246fce2203e8f2f32580f2d947312a09&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = WalletConnect.Params.Pair(uri)

            val listener = object : WalletConnectClient.WalletDelegate {
                override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62" }
                    val approveParams: WalletConnect.Params.Approve = WalletConnect.Params.Approve(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams)
                }

                override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {}
                override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {}
                override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {}

                override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
                    Logger.log("Pairing settle response: $response")
                }

                override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
                    Logger.log("Session settle response: $response")

                    if (response is WalletConnect.Model.SettledSessionResponse.Result) {

                        val updateParams = WalletConnect.Params.UpdateAccounts(
                            response.session.topic,
                            WalletConnect.Model.SessionState(accounts = listOf("eip155:8001:0x022c0c42a80bd19EA4cF0F94c4F9F96645759716"))
                        )

                        WalletConnectClient.updateSessionAccounts(updateParams)
                    }
                }

                override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {}
                override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {
                    if (response is WalletConnect.Model.SessionUpdateAccountsResponse.Result) {
                        assert(true)
                        activityRule.close()
                    } else {
                        assert(false)
                        activityRule.close()
                    }
                }
            }

            WalletConnectClient.setWalletDelegate(listener)
            WalletConnectClient.pair(pairingParams)
        }
    }

    @Test
    fun responderSendSessionPingTest() {
        activityRule.launch {
            val initParams = WalletConnect.Params.Init(
                application = app,
                useTls = true,
                hostName = "relay.walletconnect.org",
                projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                metadata = metadata
            )
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:776558f58c4a41273d6a7dee4404eb58bed4b42949370e06680992f4916ca600@2?controller=false&publicKey=fe0ae6439d3b3fa8aaf6e478bd3e6d4528ec21cb595bb73f6c9c62b1e5135b23&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = WalletConnect.Params.Pair(uri)

            val listener = object : WalletConnectClient.WalletDelegate {
                override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62" }
                    val approveParams: WalletConnect.Params.Approve = WalletConnect.Params.Approve(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams)
                }

                override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {}
                override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {}
                override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {}

                override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
                    Logger.log("Pairing settle response: $response")
                }

                override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
                    Logger.log("Session settle response: $response")

                    if (response is WalletConnect.Model.SettledSessionResponse.Result) {
                        val pingParams = WalletConnect.Params.Ping(response.session.topic)

                        WalletConnectClient.ping(pingParams, object : WalletConnect.Listeners.SessionPing {
                            override fun onSuccess(topic: String) {
                                assert(true)
                                activityRule.close()
                            }

                            override fun onError(error: Throwable) {
                                assert(false)
                                activityRule.close()
                            }
                        })
                    }
                }

                override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {}
                override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {}
            }

            WalletConnectClient.setWalletDelegate(listener)
            WalletConnectClient.pair(pairingParams)
        }
    }

    @Test
    fun responderSendNotificationTest() {
        activityRule.launch {
            val initParams = WalletConnect.Params.Init(
                application = app,
                useTls = true,
                hostName = "relay.walletconnect.org",
                projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                metadata = metadata
            )
            WalletConnectClient.initialize(initParams)


            val uri =
                "wc:8d45a8b64d4b921ee8608053ebbbea7a52d8c59ded79f379a868f524c868789f@2?controller=false&publicKey=8d18b02dbbd8c29133255a847061af36a7673ebdcdbf0a05aaac3a3ef7391703&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = WalletConnect.Params.Pair(uri)

            val listener = object : WalletConnectClient.WalletDelegate {
                override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62" }
                    val approveParams: WalletConnect.Params.Approve = WalletConnect.Params.Approve(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams)
                }

                override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {}
                override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {}
                override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {}

                override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
                    Logger.log("Pairing settle response: $response")
                }

                override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
                    Logger.log("Session settle response: $response")

                    if (response is WalletConnect.Model.SettledSessionResponse.Result) {
                        val notificationParams = WalletConnect.Params.Emit(
                            response.session.topic,
                            WalletConnect.Model.Event("type", "test")
                        )

                        WalletConnectClient.emit(notificationParams)
                    }
                }

                override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {}
                override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {}
            }

            WalletConnectClient.setWalletDelegate(listener)
            WalletConnectClient.pair(pairingParams)
        }
    }

    @Test
    fun responderSessionExtendTest() {
        activityRule.launch {
            val initParams = WalletConnect.Params.Init(
                application = app,
                useTls = true,
                hostName = "relay.walletconnect.org",
                projectId = "2ee94aca5d98e6c05c38bce02bee952a",
                metadata = metadata
            )
            WalletConnectClient.initialize(initParams)

            val uri =
                "wc:d948c3ca8b323d9711fd304ba285a1835d91d5c71ea70e5a894597dadf7322e4@2?controller=false&publicKey=e28a8553a79655d6a350ff2099b95913831d7ee671ef540af706d4eb9d2d6949&relay=%7B%22protocol%22%3A%22waku%22%7D"
            val pairingParams = WalletConnect.Params.Pair(uri)

            val listener = object : WalletConnectClient.WalletDelegate {
                override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                    assert(true)
                    val accounts = sessionProposal.chains.map { chainId -> "$chainId:0xa0A6c118b1B25207A8A764E1CAe1635339bedE62" }
                    val approveParams: WalletConnect.Params.Approve = WalletConnect.Params.Approve(sessionProposal, accounts)

                    WalletConnectClient.approve(approveParams)
                }

                override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {}
                override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {}
                override fun onSessionEvent(sessionEvent: WalletConnect.Model.SessionEvent) {}

                override fun onPairingSettledResponse(response: WalletConnect.Model.SettledPairingResponse) {
                    Logger.log("Pairing settle response: $response")
                }

                override fun onSessionSettleResponse(response: WalletConnect.Model.SettledSessionResponse) {
                    Logger.log("Session settle response: $response")

                    if (response is WalletConnect.Model.SettledSessionResponse.Result) {

                        val extend = WalletConnect.Params.UpdateExpiry(response.session.topic, 1646901496)
                        WalletConnectClient.updateSessionExpiry(extend)
                    }
                }

                override fun onSessionUpgradeResponse(response: WalletConnect.Model.SessionUpgradeResponse) {}
                override fun onSessionUpdateAccountsResponse(response: WalletConnect.Model.SessionUpdateAccountsResponse) {}
            }

            WalletConnectClient.setWalletDelegate(listener)
            WalletConnectClient.pair(pairingParams)
        }
    }
}