package com.walletconnect.sign.test.client

import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.activity.WCInstrumentedActivityScenario
import com.walletconnect.sign.test.utils.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import timber.log.Timber

@ExtendWith(WCInstrumentedActivityScenario::class)
class SignClientInstrumentedAndroidTest {

    val scenarioExtension = WCInstrumentedActivityScenario

    private fun setDelegates(walletDelegate: SignClient.WalletDelegate, dappDelegate: SignClient.DappDelegate) {
        WalletSignClient.setWalletDelegate(walletDelegate)
        DappSignClient.setDappDelegate(dappDelegate)
    }

    @Test
    fun pair() {
        setDelegates(WalletDelegate(), DappDelegate())

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pair() { scenarioExtension.closeAsSuccess() } }
    }

    @Test
    fun establishSession() {
        val walletDelegate = AutoApproveSessionWalletDelegate()
        val dappDelegate = AutoApproveDappDelegate { scenarioExtension.closeAsSuccess() }
        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairAndConnect() }
    }

    @Test
    fun receiveRejectSession() {
        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
                sessionProposal.rejectOnSessionProposal()
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionRejected(rejectedSession: Sign.Model.RejectedSession) {
                scenarioExtension.closeAsSuccess()
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairAndConnect() }
    }

    @Test
    fun receiveDisconnectSessionFromDapp() {
        val walletDelegate = object : AutoApproveSessionWalletDelegate() {
            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                scenarioExtension.closeAsSuccess()
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

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong())
        { pairAndConnect() }
    }

    @Test
    fun receiveDisconnectSessionFromWallet() {
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
                scenarioExtension.closeAsSuccess()
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairAndConnect() }
    }

    @Test
    fun receiveRespondWithResultToSessionRequest() {
        val walletDelegate = object : AutoApproveSessionWalletDelegate() {
            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {
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
                    is Sign.Model.JsonRpcResponse.JsonRpcResult -> scenarioExtension.closeAsSuccess()
                }
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairAndConnect() }
    }

    @Test
    fun receiveRespondWithErrorToSessionRequest() {
        val walletDelegate = object : AutoApproveSessionWalletDelegate() {
            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {
                walletClientRespondToRequest(sessionRequest.topic, Sign.Model.JsonRpcResponse.JsonRpcError(sessionRequest.request.id, 0, "test error"))
            }
        }

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            dappClientSendRequest(approvedSession.topic)
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
                when (response.result) {
                    is Sign.Model.JsonRpcResponse.JsonRpcError -> scenarioExtension.closeAsSuccess()
                    is Sign.Model.JsonRpcResponse.JsonRpcResult -> fail("Expected error response not result")
                }
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairAndConnect() }
    }

    @Test
    fun receiveSessionUpdate() {
        val walletDelegate = AutoApproveSessionWalletDelegate()

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            walletClientUpdateSession(approvedSession.topic)
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionUpdate(updatedSession: Sign.Model.UpdatedSession) {
                assert(updatedSession.namespaces[sessionNamespaceKey]?.chains?.size == sessionChains.size + 1)
                scenarioExtension.closeAsSuccess()
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairAndConnect() }
    }

    @Test
    fun receiveSessionEvent() {
        val walletDelegate = AutoApproveSessionWalletDelegate()

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            walletClientEmitEvent(approvedSession.topic)
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionEvent(sessionEvent: Sign.Model.SessionEvent) {
                assert(sessionEvent.name == sessionEvents.first())
                assert(sessionEvent.data == "dummy")
                scenarioExtension.closeAsSuccess()
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairAndConnect() }
    }

    @Test
    fun receiveSessionExtend() {
        val walletDelegate = AutoApproveSessionWalletDelegate()

        val onSessionApprovedSuccess = { approvedSession: Sign.Model.ApprovedSession ->
            walletClientExtendSession(approvedSession.topic)
        }

        val dappDelegate = object : AutoApproveDappDelegate(onSessionApprovedSuccess) {
            override fun onSessionExtend(session: Sign.Model.Session) {
                scenarioExtension.closeAsSuccess()
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) { pairAndConnect() }
    }
}