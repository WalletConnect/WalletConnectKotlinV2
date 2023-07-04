package com.walletconnect.sign.test.client

import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.activity.WCInstrumentedActivityScenario
import com.walletconnect.sign.test.utils.AutoApproveDappDelegate
import com.walletconnect.sign.test.utils.AutoApproveSessionWalletDelegate
import com.walletconnect.sign.test.utils.DappDelegate
import com.walletconnect.sign.test.utils.DappSignClient
import com.walletconnect.sign.test.utils.WalletDelegate
import com.walletconnect.sign.test.utils.WalletSignClient
import com.walletconnect.sign.test.utils.dappClientSendRequest
import com.walletconnect.sign.test.utils.globalOnError
import com.walletconnect.sign.test.utils.pair
import com.walletconnect.sign.test.utils.pairAndConnect
import com.walletconnect.sign.test.utils.rejectOnSessionProposal
import com.walletconnect.sign.test.utils.sessionChains
import com.walletconnect.sign.test.utils.sessionEvents
import com.walletconnect.sign.test.utils.sessionNamespaceKey
import com.walletconnect.sign.test.utils.walletClientEmitEvent
import com.walletconnect.sign.test.utils.walletClientExtendSession
import com.walletconnect.sign.test.utils.walletClientRespondToRequest
import com.walletconnect.sign.test.utils.walletClientUpdateSession
import junit.framework.TestCase.fail
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

class SignClientInstrumentedAndroidTest {

    @get:Rule
    val scenarioExtension = WCInstrumentedActivityScenario()

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

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pair() { scenarioExtension.closeAsSuccess().also { Timber.d("pair: finish") } } }
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
    fun receiveSessionExtend() {
        Timber.d("receiveSessionExtend: start")

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
}