package com.walletconnect.sign.test.client

import com.walletconnect.android.Core
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.activity.WCIntegrationActivityScenario
import com.walletconnect.sign.test.utils.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import timber.log.Timber

@ExtendWith(WCIntegrationActivityScenario::class)
class SignClientIntegrationAndroidTest {

    val scenarioExtension = WCIntegrationActivityScenario

    private fun setDelegates(walletDelegate: SignClient.WalletDelegate, dappDelegate: SignClient.DappDelegate) {
        WalletSignClient.setWalletDelegate(walletDelegate)
        DappSignClient.setDappDelegate(dappDelegate)
    }

    @Test
    fun pair() {
        setDelegates(WalletDelegate(), DappDelegate())

        scenarioExtension.launch(10L) {
            TestClient.Dapp.Pairing.getPairings().let { pairings ->
                if (pairings.isEmpty()) {
                    Timber.d("pairings.isEmpty() == true")

                    val pairing = TestClient.Dapp.Pairing.create(onError = ::globalOnError) ?: fail("Unable to create a Pairing")
                    Timber.d("DappClient.pairing.create: $pairing")

                    TestClient.Wallet.Pairing.pair(Core.Params.Pair(pairing.uri), onError = ::globalOnError, onSuccess = {
                        Timber.d("WalletClient.pairing.pair: $pairing")
                        scenarioExtension.close()
                    })
                } else {
                    Timber.d("pairings.isEmpty() == false")
                    fail("Pairing was already established. Storage must be cleared between runs")
                }
            }
        }
    }

    @Test
    fun establishSession() {
        val walletDelegate = object : WalletDelegate() {
            override fun onSessionProposal(sessionProposal: Sign.Model.SessionProposal) {
                Timber.d("walletDelegate: onSessionProposal")

                val namespaces: Map<String, Sign.Model.Namespace.Session> = mapOf(
                    "eip155" to Sign.Model.Namespace.Session(listOf("eip155:1"), listOf("eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"), listOf("someMethod"), listOf("someEvent"))
                )

                WalletSignClient.approveSession(Sign.Params.Approve(sessionProposal.proposerPublicKey, namespaces), onSuccess = {}, onError = ::globalOnError)
                Timber.d("WalletClient: approveSession")
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionApproved(approvedSession: Sign.Model.ApprovedSession) {
                Timber.d("dappDelegate: onSessionApproved")

                DappSignClient.ping(Sign.Params.Ping(approvedSession.topic), object : Sign.Listeners.SessionPing {
                    override fun onSuccess(pingSuccess: Sign.Model.Ping.Success) {
                        Timber.d("dappDelegate: onPingSuccess")
                        scenarioExtension.close()
                    }

                    override fun onError(pingError: Sign.Model.Ping.Error) {
                        fail(pingError.error)
                    }
                })
            }
        }

        setDelegates(walletDelegate, dappDelegate)

        scenarioExtension.launch(10L) {
            val namespaces: Map<String, Sign.Model.Namespace.Proposal> = mapOf("eip155" to Sign.Model.Namespace.Proposal(listOf("eip155:1"), listOf("someMethod"), listOf("someEvent")))

            val dappClientConnect = { pairing: Core.Model.Pairing ->
                val connectParams = Sign.Params.Connect(namespaces = namespaces, optionalNamespaces = null, properties = null, pairing = pairing)
                DappSignClient.connect(
                    connectParams,
                    onSuccess = { Timber.d("DappClient: connect onSuccess") },
                    onError = ::globalOnError
                )
            }

            TestClient.Dapp.Pairing.getPairings().let { pairings ->
                if (pairings.isEmpty()) {
                    Timber.d("pairings.isEmpty() == true")

                    val pairing = TestClient.Dapp.Pairing.create(onError = ::globalOnError) ?: fail("Unable to create a Pairing")
                    Timber.d("DappClient.pairing.create: $pairing")

                    TestClient.Wallet.Pairing.pair(Core.Params.Pair(pairing.uri), onError = ::globalOnError, onSuccess = {
                        Timber.d("WalletClient.pairing.pair: $pairing")
                        dappClientConnect(pairing)
                    })
                } else {
                    Timber.d("pairings.isEmpty() == false")
                    dappClientConnect(pairings.first())
                }
            }
        }
    }
}