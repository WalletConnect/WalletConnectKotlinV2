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

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pair() { scenarioExtension.closeAsSuccess() }
        }
    }

    @Test
    fun establishSession() {
        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
                sessionProposal.approveOnSessionProposal()
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                approvedSession.onSessionApproved { scenarioExtension.closeAsSuccess() }
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairAndConnect()
        }
    }

    @Test
    fun rejectSession() {
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

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairAndConnect()
        }
    }

    @Test
    fun disconnectSessionFromDapp() {
        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
                sessionProposal.approveOnSessionProposal()
            }

            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                scenarioExtension.closeAsSuccess()
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                approvedSession.onSessionApproved {
                    DappSignClient.disconnect(
                        Sign.Params.Disconnect(approvedSession.topic),
                        onSuccess = { Timber.d("Dapp: disconnectOnSuccess") },
                        onError = ::globalOnError
                    )
                }
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairAndConnect()
        }
    }

    @Test
    fun disconnectSessionFromWallet() {
        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
                sessionProposal.approveOnSessionProposal()
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                approvedSession.onSessionApproved {
                    WalletSignClient.disconnect(
                        Sign.Params.Disconnect(approvedSession.topic),
                        onSuccess = { Timber.d("Wallet: disconnectOnSuccess") },
                        onError = ::globalOnError
                    )
                }
            }

            override fun onSessionDelete(deletedSession: Sign.Model.DeletedSession) {
                scenarioExtension.closeAsSuccess()
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairAndConnect()
        }
    }

    @Test
    fun respondWithResultToSessionRequest() {
        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
                sessionProposal.approveOnSessionProposal()
            }

            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {
                respondToRequest(sessionRequest.topic, Sign.Model.JsonRpcResponse.JsonRpcResult(sessionRequest.request.id, "dummy"))
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                approvedSession.onSessionApproved {
                    dappClientSendRequest(approvedSession.topic)
                }
            }

            override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
                when (response.result) {
                    is Sign.Model.JsonRpcResponse.JsonRpcError -> fail("Expected result response not error")
                    is Sign.Model.JsonRpcResponse.JsonRpcResult -> scenarioExtension.closeAsSuccess()
                }
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairAndConnect()
        }
    }

    @Test
    fun respondWithErrorToSessionRequest() {
        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
                sessionProposal.approveOnSessionProposal()
            }

            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest) {
                respondToRequest(sessionRequest.topic, Sign.Model.JsonRpcResponse.JsonRpcError(sessionRequest.request.id, 0, "test error"))
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                approvedSession.onSessionApproved {
                    dappClientSendRequest(approvedSession.topic)
                }
            }

            override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
                when (response.result) {
                    is Sign.Model.JsonRpcResponse.JsonRpcError -> scenarioExtension.closeAsSuccess()
                    is Sign.Model.JsonRpcResponse.JsonRpcResult -> fail("Expected error response not result")
                }
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(BuildConfig.TEST_TIMEOUT_SECONDS.toLong()) {
            pairAndConnect()
        }
    }
}