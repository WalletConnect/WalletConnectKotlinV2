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
import com.walletconnect.sign.test.utils.wallet.AutoApproveSessionWalletDelegate
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
    fun pairDappWithHybriApp() {
        Timber.d("Pair Dapp with HybridWallet: start pairing")

        setDelegates(WalletDelegate(), DappDelegate(), HybridAppWalletDelegate(), HybridAppDappDelegate())
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairDappWithHybrid {
                scenarioExtension.closeAsSuccess().also { Timber.d("Pair Dapp with HybridWallet: finish pairing") }
            }
        }
    }

//    @Test
//    fun pairDappWithHybriApp() {
//        Timber.d("Pair Dapp with HybridWallet: start pairing")
//
//        setDelegates(WalletDelegate(), DappDelegate(), HybridAppWalletDelegate(), HybridAppDappDelegate())
//        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
//            pairDappWithHybrid {
//                scenarioExtension.closeAsSuccess().also { Timber.d("Pair Dapp with HybridWallet: finish pairing") }
//            }
//        }
//    }

    @Test
    fun pairHybridAppWithWallet() {
        Timber.d("Pair HybridApp with Wallet: start pairing")

        setDelegates(WalletDelegate(), DappDelegate(), HybridAppWalletDelegate(), HybridAppDappDelegate())
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairHybridWithWallet {
                scenarioExtension.closeAsSuccess().also { Timber.d("Pair HybridApp with Wallet: finish pairing") }
            }
        }
    }

    @Test
    fun establishSessionBetweenDappAndHybridApp() {
        Timber.d("Establish session between Dapp and hybrid wallet: start")

        val approveSessionWalletDelegate = object : HybridAppWalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("HybridAppWalletDelegate: onSessionProposal: $sessionProposal")

                HybridSignClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, sessionNamespaces), onSuccess = {}, onError = ::globalOnError)
                Timber.d("HybridAppWalletDelegate: approveSession")
            }
        }

        val sessionApproveDappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                scenarioExtension.closeAsSuccess().also { Timber.d("DappDelegate: establishSession finish") }
            }
        }

        setDelegates(WalletDelegate(), sessionApproveDappDelegate, approveSessionWalletDelegate, HybridAppDappDelegate())
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairDappWithHybrid { pairing -> dappClientConnect(pairing) } }
    }

//    @Test
//    fun establishSessionBetweenDappAndHybridAppAndWallet() {
//        Timber.d("Establish session between Dapp and hybrid wallet: start")
//
//        val approveSessionWalletDelegate = object : HybridAppWalletDelegate() {
//            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
//                Timber.d("HybridAppWalletDelegate: onSessionProposal: $sessionProposal")
//
//                HybridSignClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, sessionNamespaces), onSuccess = {}, onError = ::globalOnError)
//                Timber.d("HybridAppWalletDelegate: approveSession")
//            }
//        }
//
//        val sessionApproveDappDelegate = object : DappDelegate() {
//            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
//                scenarioExtension.closeAsSuccess().also { Timber.d("DappDelegate: establishSession finish") }
//            }
//        }
//
//        setDelegates(WalletDelegate(), sessionApproveDappDelegate, approveSessionWalletDelegate, HybridAppDappDelegate())
//        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairDappWithHybrid { pairing -> dappClientConnect(pairing) } }
//    }

    @Test
    fun establishSessionBetweenHybridAppAndWallet() {
        Timber.d("Establish session between hybrid Dapp and wallet: start")

        val approveSessionWalletDelegate = AutoApproveSessionWalletDelegate()
        val sessionApproveDappDelegate = object : HybridAppDappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                scenarioExtension.closeAsSuccess().also { Timber.d("HybridAppDappDelegate: establishSession finish") }
            }
        }

        setDelegates(approveSessionWalletDelegate, DappDelegate(), HybridAppWalletDelegate(), sessionApproveDappDelegate)
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairHybridWithWallet { pairing -> hybridClientConnect(pairing) } }
    }

    @Test
    fun receiveRejectSessionOnDappFromHybdridApp() {
        Timber.d("Receive Reject Session On Hybrid Wallet From External Dapp: start")

        val walletHybridDelegate = object : HybridAppWalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("HybridAppWalletDelegate: onSessionProposal: $sessionProposal")

                HybridSignClient.rejectSession(Sign.Params.Reject(sessionProposal.proposerPublicKey, "test reason"), onSuccess = {}, onError = ::globalOnError)
                Timber.d("HybridSignClient: rejectSession")
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
                scenarioExtension.closeAsSuccess().also { Timber.d("receiveRejectSession: finish") }
            }
        }
        setDelegates(WalletDelegate(), dappDelegate, walletHybridDelegate, HybridAppDappDelegate())
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairDappWithHybrid { pairing -> dappClientConnect(pairing) } }
    }

    @Test
    fun receiveRejectSessionOnHybridAppFromWallet() {
        Timber.d("Receive Reject Session On HybridApp From Wallet: start")

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("WalletDelegate: onSessionProposal: $sessionProposal")

                WalletSignClient.rejectSession(Sign.Params.Reject(sessionProposal.proposerPublicKey, "test reason"), onSuccess = {}, onError = ::globalOnError)
                Timber.d("WalletSignClient: rejectSession")
            }
        }

        val dappHybridDelegate = object : HybridAppDappDelegate() {
            override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
                scenarioExtension.closeAsSuccess().also { Timber.d("receiveRejectSession: finish") }
            }
        }
        setDelegates(walletDelegate, DappDelegate(), HybridAppWalletDelegate(), dappHybridDelegate)
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairHybridWithWallet { pairing -> hybridClientConnect(pairing) } }
    }

    @Test
    fun getActiveSessionFromHybridWallet() {
        Timber.d("Get Active Session From Hybrid Wallet: start")

        val approveSessionWalletDelegate = object : HybridAppWalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("HybridAppWalletDelegate: onSessionProposal: $sessionProposal")

                HybridSignClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, sessionNamespaces), onSuccess = {}, onError = ::globalOnError)
                Timber.d("HybridAppWalletDelegate: approveSession")
            }

            override fun onSessionSettleResponse(settleSessionResponse: Sign.Model.SettledSessionResponse) {
                val sessionTopic = (settleSessionResponse as Sign.Model.SettledSessionResponse.Result).session.topic
                HybridSignClient.getActiveSessionByTopic(sessionTopic).let { session ->
                    if (session != null) {
                        scenarioExtension.closeAsSuccess().also { Timber.d("getActiveSessionFromHybridApp: finish: $session") }
                    }
                }
            }
        }

        val sessionApproveDappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                Timber.d("DappDelegate: establishSession finish")
            }
        }

        setDelegates(WalletDelegate(), sessionApproveDappDelegate, approveSessionWalletDelegate, HybridAppDappDelegate())
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairDappWithHybrid { pairing -> dappClientConnect(pairing) } }
    }

    @Test
    fun getActiveSessionFromHybridDapp() {
        Timber.d("Get Active Session From Hybrid Dapp: start")

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("WalletDelegate: onSessionProposal: $sessionProposal")

                WalletSignClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, sessionNamespaces), onSuccess = {}, onError = ::globalOnError)
                Timber.d("WalletSignClient: approveSession")
            }
        }

        val sessionApproveDappDelegate = object : HybridAppDappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                val sessionTopic = approvedSession.topic
                HybridSignClient.getActiveSessionByTopic(sessionTopic).let { session ->
                    if (session != null) {
                        scenarioExtension.closeAsSuccess().also { Timber.d("getActiveSessionFromHybridApp: finish: $session") }
                    }
                }
            }
        }

        setDelegates(walletDelegate, DappDelegate(), HybridAppWalletDelegate(), sessionApproveDappDelegate)
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairHybridWithWallet { pairing -> hybridClientConnect(pairing) } }
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

    private fun pairHybridWithWallet(onSuccess: (pairing: Core.Model.Pairing) -> Unit) {
        TestClient.Hybrid.Pairing.getPairings().let { pairings ->
            if (pairings.isEmpty()) {
                Timber.d("pairings.isEmpty() == true")
                val pairing: Core.Model.Pairing = (TestClient.Hybrid.Pairing.create(onError = ::globalOnError) ?: TestCase.fail("Unable to create a Pairing")) as Core.Model.Pairing
                Timber.d("HybridApp.pairing.create: $pairing")

                TestClient.Wallet.Pairing.pair(Core.Params.Pair(pairing.uri), onError = ::globalOnError, onSuccess = {
                    Timber.d("WalletClient.pairing.pair: $pairing")
                    onSuccess(pairing)
                })
            } else {
                Timber.d("pairings.isEmpty() == false")
                TestCase.fail("Pairing already exists. Storage must be cleared in between runs")
            }
        }
    }

    private fun pairDappWithHybrid(onSuccess: (pairing: Core.Model.Pairing) -> Unit) {
        TestClient.Dapp.Pairing.getPairings().let { pairings ->
            if (pairings.isEmpty()) {
                Timber.d("pairings.isEmpty() == true")
                val pairing: Core.Model.Pairing = (TestClient.Dapp.Pairing.create(onError = ::globalOnError) ?: TestCase.fail("Unable to create a Pairing")) as Core.Model.Pairing
                Timber.d("DappClient.pairing.create: $pairing")

                TestClient.Hybrid.Pairing.pair(Core.Params.Pair(pairing.uri), onError = ::globalOnError, onSuccess = {
                    Timber.d("HybridApp.pairing.pair: $pairing")
                    onSuccess(pairing)
                })
            } else {
                Timber.d("pairings.isEmpty() == false")
                TestCase.fail("Pairing already exists. Storage must be cleared in between runs")
            }
        }
    }
}