package com.walletconnect.sign.test.client

import com.walletconnect.android.Core
import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.test.scenario.SignClientInstrumentedActivityScenario
import com.walletconnect.sign.test.utils.TestClient
import com.walletconnect.sign.test.utils.dapp.DappDelegate
import com.walletconnect.sign.test.utils.dapp.DappSignClient
import com.walletconnect.sign.test.utils.dapp.dappClientAuthenticate
import com.walletconnect.sign.test.utils.globalOnError
import com.walletconnect.sign.test.utils.wallet.WalletDelegate
import com.walletconnect.sign.test.utils.wallet.WalletSignClient
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

class SessionAuthenticateInstrumentedAndroidTest {
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

    //    @Test
//    fun approveSessionAuthenticated() {
//        Timber.d("establishSession: start")
//
//        val walletDelegate = AutoApproveSessionWalletDelegate()
//        val dappDelegate = AutoApproveDappDelegate { scenarioExtension.closeAsSuccess().also { Timber.d("establishSession: finish") } }
//        launch(walletDelegate, dappDelegate)
//    }
//
    @Test
    fun rejectSessionAuthenticated() {
        Timber.d("rejectSessionAuthenticated: start")

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionAuthenticated(sessionAuthenticated: Sign.Model.SessionAuthenticated, verifyContext: Sign.Model.VerifyContext) {
                val params = Sign.Params.RejectSessionAuthenticate(sessionAuthenticated.id, "User rejections")
                WalletSignClient.rejectSessionAuthenticate(params, onSuccess = {}, onError = ::globalOnError)
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionAuthenticateResponse(sessionUpdateResponse: Sign.Model.SessionAuthenticateResponse) {
                if (sessionUpdateResponse is Sign.Model.SessionAuthenticateResponse.Error) {
                    scenarioExtension.closeAsSuccess().also { Timber.d("receiveRejectSession: finish") }
                }
            }
        }
        launch(walletDelegate, dappDelegate)
    }

    private fun pairDappAndWallet(onPairSuccess: (pairing: Core.Model.Pairing) -> Unit) {
        TestClient.Dapp.Pairing.getPairings().let { pairings ->
            if (pairings.isEmpty()) {
                Timber.d("pairings.isEmpty() == true")

                val pairing: Core.Model.Pairing = (TestClient.Dapp.Pairing.create(onError = ::globalOnError) ?: TestCase.fail("Unable to create a Pairing")) as Core.Model.Pairing
                Timber.d("DappClient.pairing.create: $pairing")

                TestClient.Wallet.Pairing.pair(Core.Params.Pair(pairing.uri), onError = ::globalOnError, onSuccess = {
                    Timber.d("WalletClient.pairing.pair: $pairing")
                    onPairSuccess(pairing)
                })
            } else {
                Timber.d("pairings.isEmpty() == false")
                TestCase.fail("Pairing already exists. Storage must be cleared in between runs")
            }
        }
    }

    private fun pairAndConnect() {
        pairDappAndWallet { pairing -> dappClientAuthenticate(pairing) }
    }
}