package com.walletconnect.sign.test.client

import com.walletconnect.android.Core
import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.scenario.HybridAppInstrumentedActivityScenario
import com.walletconnect.sign.test.utils.TestClient
import com.walletconnect.sign.test.utils.dapp.DappDelegate
import com.walletconnect.sign.test.utils.dapp.DappSignClient
import com.walletconnect.sign.test.utils.dapp.dappClientConnect
import com.walletconnect.sign.test.utils.globalOnError
import com.walletconnect.sign.test.utils.hybrid.HybridAppDappDelegate
import com.walletconnect.sign.test.utils.hybrid.HybridAppWalletDelegate
import com.walletconnect.sign.test.utils.hybrid.HybridSignClient
import com.walletconnect.sign.test.utils.hybrid.hybridClientConnect
import com.walletconnect.sign.test.utils.sessionNamespaces
import com.walletconnect.sign.test.utils.wallet.WalletDelegate
import com.walletconnect.sign.test.utils.wallet.WalletSignClient
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

class HybridAppInstrumentedAndroidTest {

    @get:Rule
    val scenarioExtension = HybridAppInstrumentedActivityScenario()

    @Test
    fun testPairingHybridAppWithDappAndWallet() {
        Timber.d("Pair Dapp with HybridApp and HybritApp with Wallet: start pairing")
        var pairingSuccessCounter = 0

        setDelegates(WalletDelegate(), DappDelegate(), HybridAppWalletDelegate(), HybridAppDappDelegate())
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairHybridAppWithDappAndWallet(
                onWalletWithHybridAppPairSuccess = {
                    pairingSuccessCounter++
                },
                onHybridAppWithDappPairSuccess = {
                    pairingSuccessCounter++
                })

            while (true) {
                if (pairingSuccessCounter == 2) {
                    scenarioExtension.closeAsSuccess().also { Timber.d("Pair Dapp with HybridApp and HybridApp with Wallet: finish pairing") }
                    break
                }
            }
        }
    }

    @Test
    fun testEstablishingSessionBetweenHybridAppAndDappAndWallet() {
        Timber.d("Establish session between HybridApp and Dapp and Wallet: start")
        var sessionApproveSuccessCounter = 0

        val approveSessionHybridAppWalletDelegate = object : HybridAppWalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("HybridAppWalletDelegate: onSessionProposal: $sessionProposal")

                HybridSignClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, sessionNamespaces), onSuccess = {}, onError = ::globalOnError)
                Timber.d("HybridAppWalletDelegate: approveSession")
            }

            override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
                Timber.d("HybridAppWalletDelegate: onSessionSettleResponse: $settleSessionResponse")
                sessionApproveSuccessCounter++
            }
        }

        val sessionApproveDappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                Timber.d("DappDelegate: establishSession finish")
            }
        }

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("WalletDelegate: onSessionProposal: $sessionProposal")

                WalletSignClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, sessionNamespaces), onSuccess = {}, onError = ::globalOnError)
                Timber.d("WalletDelegate: approveSession")
            }

            override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
                Timber.d("WalletDelegate: onSessionSettleResponse: $settleSessionResponse")
                sessionApproveSuccessCounter++
            }
        }

        val sessionApproveHybridDappDelegate = object : HybridAppDappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                Timber.d("HybridAppDappDelegate: establishSession finish")
            }
        }


        setDelegates(walletDelegate, sessionApproveDappDelegate, approveSessionHybridAppWalletDelegate, sessionApproveHybridDappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairHybridAppWithDappAndWallet(
                onWalletWithHybridAppPairSuccess = { pairing ->
                    hybridClientConnect(pairing)
                },
                onHybridAppWithDappPairSuccess = { pairing ->
                    dappClientConnect(pairing)
                })

            while (true) {
                if (sessionApproveSuccessCounter == 2) {
                    scenarioExtension.closeAsSuccess().also { Timber.d("Session established between: HybridApp and Wallet ; Dapp and HybridApp") }
                    break
                }
            }
        }
    }

    @Test
    fun receiveRejectSessionOnDappFromHybdridApp() {
        Timber.d("Receive Reject Session On Hybrid Wallet From External Dapp: start")
        var sessionRejectCounter = 0

        val rejectSessionHybridAppWalletDelegate = object : HybridAppWalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("HybridAppWalletDelegate: onSessionProposal: $sessionProposal")

                HybridSignClient.rejectSession(Sign.Params.Reject(sessionProposal.proposerPublicKey, "test reason"), onSuccess = {}, onError = ::globalOnError)
                Timber.d("HybridAppWalletDelegate: rejectSession")
            }
        }

        val sessionRejectDappDelegate = object : DappDelegate() {
            override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
                Timber.d("DappDelegate: session rejected")
                sessionRejectCounter++
            }
        }

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("WalletDelegate: onSessionProposal: $sessionProposal")

                WalletSignClient.rejectSession(Sign.Params.Reject(sessionProposal.proposerPublicKey, "test reason"), onSuccess = {}, onError = ::globalOnError)
                Timber.d("WalletDelegate: rejectSession")
            }
        }

        val sessionRejectHybridDappDelegate = object : HybridAppDappDelegate() {
            override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
                Timber.d("HybridAppDappDelegate: reject session finish")
                sessionRejectCounter++
            }
        }


        setDelegates(walletDelegate, sessionRejectDappDelegate, rejectSessionHybridAppWalletDelegate, sessionRejectHybridDappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairHybridAppWithDappAndWallet(
                onWalletWithHybridAppPairSuccess = { pairing ->
                    hybridClientConnect(pairing)
                },
                onHybridAppWithDappPairSuccess = { pairing ->
                    dappClientConnect(pairing)
                })

            while (true) {
                if (sessionRejectCounter == 2) {
                    scenarioExtension.closeAsSuccess().also { Timber.d("Session rejected on: HybridApp and Wallet ; Dapp and HybridApp") }
                    break
                }
            }
        }
    }


    @Test
    fun testGetActiveSessionFromDappHybridAppAndWallet() {
        Timber.d("Get Active Session from Dapp, HybridApp and Wallet: start")
        var successCounter = 0

        val approveSessionHybridAppWalletDelegate = object : HybridAppWalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("HybridAppWalletDelegate: onSessionProposal: $sessionProposal")

                HybridSignClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, sessionNamespaces), onSuccess = {}, onError = ::globalOnError)
                Timber.d("HybridAppWalletDelegate: approveSession")
            }

            override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
                Timber.d("HybridAppWalletDelegate: onSessionSettleResponse: $settleSessionResponse")

                val sessionTopic = (settleSessionResponse as Sign.Model.SettledSessionResponse.Result).session.topic
                HybridSignClient.getActiveSessionByTopic(sessionTopic).let { session ->
                    if (session != null) {
                        successCounter++
                    }
                }
            }
        }

        val sessionApproveDappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                Timber.d("DappDelegate: establishSession finish")

                val sessionTopic = approvedSession.topic
                DappSignClient.getActiveSessionByTopic(sessionTopic).let { session ->
                    if (session != null) {
                        successCounter++
                    }
                }
            }
        }

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("WalletDelegate: onSessionProposal: $sessionProposal")

                WalletSignClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, sessionNamespaces), onSuccess = {}, onError = ::globalOnError)
                Timber.d("WalletDelegate: approveSession")
            }

            override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
                Timber.d("WalletDelegate: onSessionSettleResponse: $settleSessionResponse")

                val sessionTopic = (settleSessionResponse as Sign.Model.SettledSessionResponse.Result).session.topic
                WalletSignClient.getActiveSessionByTopic(sessionTopic).let { session ->
                    if (session != null) {
                        successCounter++
                    }
                }
            }
        }

        val sessionApproveHybridDappDelegate = object : HybridAppDappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                Timber.d("HybridAppDappDelegate: establishSession finish")

                val sessionTopic = approvedSession.topic
                HybridSignClient.getActiveSessionByTopic(sessionTopic).let { session ->
                    if (session != null) {
                        successCounter++
                    }
                }
            }
        }


        setDelegates(walletDelegate, sessionApproveDappDelegate, approveSessionHybridAppWalletDelegate, sessionApproveHybridDappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairHybridAppWithDappAndWallet(
                onWalletWithHybridAppPairSuccess = { pairing ->
                    hybridClientConnect(pairing)
                },
                onHybridAppWithDappPairSuccess = { pairing ->
                    dappClientConnect(pairing)
                })

            while (true) {
                if (successCounter == 4) {
                    scenarioExtension.closeAsSuccess().also { Timber.d("Session established between: HybridApp and Wallet ; Dapp and HybridApp") }
                    break
                }
            }
        }
    }

    private fun setDelegates(
        walletDelegate: SignClient.WalletDelegate,
        dappDelegate: SignClient.DappDelegate,
        hybridAppWalletDelegate: HybridAppWalletDelegate,
        hybridAppDappDelegate: HybridAppDappDelegate
    ) {
        WalletSignClient.setWalletDelegate(walletDelegate)
        DappSignClient.setDappDelegate(dappDelegate)

        HybridSignClient.setWalletDelegate(hybridAppWalletDelegate)
        HybridSignClient.setDappDelegate(hybridAppDappDelegate)
    }

    private fun pairHybridAppWithDappAndWallet(onWalletWithHybridAppPairSuccess: (pairing: Core.Model.Pairing) -> Unit, onHybridAppWithDappPairSuccess: (pairing: Core.Model.Pairing) -> Unit) {
        TestClient.Hybrid.Pairing.getPairings().let { pairings ->
            if (pairings.isEmpty()) {
                Timber.d("pairings.isEmpty() == true")

                val hybridPairing: Core.Model.Pairing = (TestClient.Hybrid.Pairing.create(onError = ::globalOnError) ?: TestCase.fail("Unable to create a Pairing")) as Core.Model.Pairing
                TestClient.Wallet.Pairing.pair(Core.Params.Pair(hybridPairing.uri), onError = ::globalOnError, onSuccess = {
                    Timber.d("Wallet.pairing.pair: $hybridPairing")
                    onWalletWithHybridAppPairSuccess(hybridPairing)
                })

                val pairing: Core.Model.Pairing = (TestClient.Dapp.Pairing.create(onError = ::globalOnError) ?: TestCase.fail("Unable to create a Pairing")) as Core.Model.Pairing
                Timber.d("DappClient.pairing.create: $pairing")
                TestClient.Hybrid.Pairing.pair(Core.Params.Pair(pairing.uri), onError = ::globalOnError, onSuccess = {
                    Timber.d("HybridApp.pairing.pair: $pairing")
                    onHybridAppWithDappPairSuccess(pairing)
                })

            } else {
                Timber.d("pairings.isEmpty() == false")
                TestCase.fail("Pairing already exists. Storage must be cleared in between runs")
            }
        }
    }
}