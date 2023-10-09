package com.walletconnect.sign.test.client

import com.walletconnect.android.Core
import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.scenario.SignClientInstrumentedActivityScenario
import com.walletconnect.sign.test.utils.TestClient
import com.walletconnect.sign.test.utils.dapp.AutoApproveDappDelegate
import com.walletconnect.sign.test.utils.dapp.DappDelegate
import com.walletconnect.sign.test.utils.dapp.DappSignClient
import com.walletconnect.sign.test.utils.dapp.dappClientConnect
import com.walletconnect.sign.test.utils.dapp.dappClientSendRequest
import com.walletconnect.sign.test.utils.globalOnError
import com.walletconnect.sign.test.utils.sessionChains
import com.walletconnect.sign.test.utils.sessionEvents
import com.walletconnect.sign.test.utils.sessionNamespaceKey
import com.walletconnect.sign.test.utils.wallet.AutoApproveSessionWalletDelegate
import com.walletconnect.sign.test.utils.wallet.WalletDelegate
import com.walletconnect.sign.test.utils.wallet.WalletSignClient
import com.walletconnect.sign.test.utils.wallet.dappClientExtendSession
import com.walletconnect.sign.test.utils.wallet.rejectOnSessionProposal
import com.walletconnect.sign.test.utils.wallet.walletClientEmitEvent
import com.walletconnect.sign.test.utils.wallet.walletClientExtendSession
import com.walletconnect.sign.test.utils.wallet.walletClientRespondToRequest
import com.walletconnect.sign.test.utils.wallet.walletClientUpdateSession
import junit.framework.TestCase.fail
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

class SignClientInstrumentedAndroidTest {
    @get:Rule
    val scenarioExtension = SignClientInstrumentedActivityScenario()

    private fun setDelegates(walletDelegate: SignClient.WalletDelegate, dappDelegate: SignClient.DappDelegate) {
        WalletSignClient.setWalletDelegate(walletDelegate)
        DappSignClient.setDappDelegate(dappDelegate)
    }

    private fun launch(walletDelegate: SignClient.WalletDelegate, dappDelegate: SignClient.DappDelegate) {
        setDelegates(walletDelegate, dappDelegate)
        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairAndConnect() }
    }

    @Test
    fun pair() {
        Timber.d("pair: start")
        setDelegates(WalletDelegate(), DappDelegate())

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairDappAndWallet { scenarioExtension.closeAsSuccess().also { Timber.d("pair: finish") } } }
    }

    @Test
    fun establishSession() {
        Timber.d("establishSession: start")

        val walletDelegate = AutoApproveSessionWalletDelegate()
        val dappDelegate = AutoApproveDappDelegate { scenarioExtension.closeAsSuccess().also { Timber.d("establishSession: finish") } }
        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun receiveRejectSession() {
        Timber.d("receiveRejectSession: start")

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal, verifyContext: Sign.Model.VerifyContext) {
                sessionProposal.rejectOnSessionProposal()
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
                scenarioExtension.closeAsSuccess().also { Timber.d("receiveRejectSession: finish") }
            }
        }
        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun receiveDisconnectSessionFromDapp() {
        Timber.d("receiveDisconnectSessionFromDapp: start")

        val walletDelegate = object : AutoApproveSessionWalletDelegate() {
            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                scenarioExtension.closeAsSuccess().also { Timber.d("receiveDisconnectSessionFromDapp: finish") }
            }
        }

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            DappSignClient.disconnect(
                Sign.Params.Disconnect(approvedSession.topic),
                onSuccess = { Timber.d("Dapp: disconnectOnSuccess") },
                onError = ::globalOnError
            )
        }

        val dappDelegate = AutoApproveDappDelegate(onSessionApprovedSuccess)

        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun receiveDisconnectSessionFromWallet() {
        Timber.d("receiveDisconnectSessionFromWallet: start")

        val walletDelegate = AutoApproveSessionWalletDelegate()

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            WalletSignClient.disconnect(
                Sign.Params.Disconnect(approvedSession.topic),
                onSuccess = { Timber.d("Wallet: disconnectOnSuccess") },
                onError = ::globalOnError
            )
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                scenarioExtension.closeAsSuccess().also { Timber.d("receiveDisconnectSessionFromWallet: finish") }
            }
        }
        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun receiveRespondWithResultToSessionRequest() {
        Timber.d("receiveRespondWithResultToSessionRequest: start")

        val walletDelegate = object : AutoApproveSessionWalletDelegate() {
            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, verifyContext: Sign.Model.VerifyContext) {
                walletClientRespondToRequest(sessionRequest.topic, Sign.Model.JsonRpcResponse.JsonRpcResult(sessionRequest.request.id, "dummy"))
            }
        }

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            dappClientSendRequest(approvedSession.topic)
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
                when (response.result) {
                    is Sign.Model.JsonRpcResponse.JsonRpcError -> fail("Expected result response not error")
                    is Sign.Model.JsonRpcResponse.JsonRpcResult -> {
                        // Validate the result
                        scenarioExtension.closeAsSuccess().also { Timber.d("receiveRespondWithResultToSessionRequest: finish") }
                    }
                }
            }
        }
        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun receiveRespondWithErrorToSessionRequest() {
        Timber.d("receiveRespondWithErrorToSessionRequest: start")

        val walletDelegate = object : AutoApproveSessionWalletDelegate() {
            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, verifyContext: Sign.Model.VerifyContext) {
                walletClientRespondToRequest(sessionRequest.topic, Sign.Model.JsonRpcResponse.JsonRpcError(sessionRequest.request.id, 0, "test error"))
            }
        }

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            dappClientSendRequest(approvedSession.topic)
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
                when (response.result) {
                    is Sign.Model.JsonRpcResponse.JsonRpcError -> scenarioExtension.closeAsSuccess().also { Timber.d("receiveRespondWithErrorToSessionRequest: finish") }
                    is Sign.Model.JsonRpcResponse.JsonRpcResult -> fail("Expected error response not result")
                }
            }
        }
        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun receiveSessionUpdate() {
        Timber.d("receiveSessionUpdate: start")

        val walletDelegate = AutoApproveSessionWalletDelegate()

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            walletClientUpdateSession(approvedSession.topic)
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {
                assert(updatedSession.namespaces[sessionNamespaceKey]?.chains?.size == sessionChains.size + 1)
                scenarioExtension.closeAsSuccess().also { Timber.d("receiveSessionUpdate: finish") }
            }
        }
        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun receiveSessionEvent() {
        Timber.d("receiveSessionEvent: start")

        val walletDelegate = AutoApproveSessionWalletDelegate()

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            walletClientEmitEvent(approvedSession.topic)
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {
                assert(sessionEvent.name == sessionEvents.first())
                assert(sessionEvent.data == "dummy")
                scenarioExtension.closeAsSuccess().also { Timber.d("receiveSessionEvent: finish") }
            }
        }
        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun extendSessionByWallet() {
        Timber.d("receiveSessionExtendByWallet: start")

        val walletDelegate = AutoApproveSessionWalletDelegate()

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            walletClientExtendSession(approvedSession.topic)
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionExtend(session: Sign.Model.Session) {
                scenarioExtension.closeAsSuccess().also { Timber.d("receiveSessionExtend: finish") }
            }
        }

        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun extendSessionByDapp() {
        Timber.d("receiveSessionExtendByDapp: start")

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            Timber.d("session approved: ${approvedSession.topic}")
            dappClientExtendSession(approvedSession.topic)
        }

        val dappDelegate = AutoApproveDappDelegate(onSessionApprovedSuccess)

        val walletDelegate = object : AutoApproveSessionWalletDelegate() {
            override fun onSessionExtend(session: Sign.Model.Session) {
                scenarioExtension.closeAsSuccess().also { Timber.d("Wallet receiveSessionExtend: finish: ${session.expiry}") }
            }
        }

        launch(walletDelegate, dappDelegate)
    }

    private fun pairDappAndWallet(onPairSuccess: (pairing: Core.Model.Pairing) -> Unit) {
        TestClient.Dapp.Pairing.getPairings().let { pairings ->
            if (pairings.isEmpty()) {
                Timber.d("pairings.isEmpty() == true")

                val pairing: Core.Model.Pairing = (TestClient.Dapp.Pairing.create(onError = ::globalOnError) ?: fail("Unable to create a Pairing")) as Core.Model.Pairing
                Timber.d("DappClient.pairing.create: $pairing")

                TestClient.Wallet.Pairing.pair(Core.Params.Pair(pairing.uri), onError = ::globalOnError, onSuccess = {
                    Timber.d("WalletClient.pairing.pair: $pairing")
                    onPairSuccess(pairing)
                })
            } else {
                Timber.d("pairings.isEmpty() == false")
                fail("Pairing already exists. Storage must be cleared in between runs")
            }
        }
    }

    private fun pairAndConnect() {
        pairDappAndWallet { pairing -> dappClientConnect(pairing) }
    }
}