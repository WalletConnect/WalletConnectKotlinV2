package com.walletconnect.sign.test.client

import com.walletconnect.android.Core
import com.walletconnect.android.cacao.signature.SignatureType
import com.walletconnect.android.utils.cacao.signHex
import com.walletconnect.sign.BuildConfig
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import com.walletconnect.sign.client.utils.CacaoSigner
import com.walletconnect.sign.client.utils.generateCACAO
import com.walletconnect.sign.test.scenario.SignClientInstrumentedActivityScenario
import com.walletconnect.sign.test.utils.TestClient
import com.walletconnect.sign.test.utils.dapp.DappDelegate
import com.walletconnect.sign.test.utils.dapp.DappSignClient
import com.walletconnect.sign.test.utils.dapp.dappClientAuthenticate
import com.walletconnect.sign.test.utils.dapp.dappClientSendRequest
import com.walletconnect.sign.test.utils.globalOnError
import com.walletconnect.sign.test.utils.wallet.WalletDelegate
import com.walletconnect.sign.test.utils.wallet.WalletSignClient
import com.walletconnect.sign.test.utils.wallet.walletClientRespondToRequest
import com.walletconnect.util.hexToBytes
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.web3j.utils.Numeric
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
    fun approveSessionAuthenticated() {
        Timber.d("approveSessionAuthenticated: start")

        val (privateKey, address) = Pair("fc38e74680851b8d0c2dc69ccd367d4c0d963a4065dff56a87f450eef33336c4", "0xF983704E5A9eF14C32e8fe751b34E61702437aBF")

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionAuthenticate(sessionAuthenticate: Sign.Model.SessionAuthenticate, verifyContext: Sign.Model.VerifyContext) {
                val issuerToMessages = mutableListOf<Pair<String, String>>()
                val cacaos = mutableListOf<Sign.Model.Cacao>()

                sessionAuthenticate.payloadParams.chains.forEach { chain ->
                    val issuer = "did:pkh:$chain:$address"
                    val message = WalletSignClient.formatAuthMessage(Sign.Params.FormatMessage(sessionAuthenticate.payloadParams, issuer)) ?: throw Exception("Invalid message")
                    issuerToMessages.add(issuer to message)
                }

                issuerToMessages.forEach { issuerToMessage ->
                    val messageToSign = Numeric.toHexString(issuerToMessage.second.toByteArray())
                    val signature = CacaoSigner.signHex(messageToSign, privateKey.hexToBytes(), SignatureType.EIP191)
                    val cacao = generateCACAO(sessionAuthenticate.payloadParams, issuerToMessage.first, signature)
                    cacaos.add(cacao)
                }

                val params = Sign.Params.ApproveSessionAuthenticate(sessionAuthenticate.id, cacaos)
                WalletSignClient.approveSessionAuthenticated(params, onSuccess = {}, onError = ::globalOnError)
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Sign.Model.SessionAuthenticateResponse) {
                if (sessionAuthenticateResponse is Sign.Model.SessionAuthenticateResponse.Result) {
                    scenarioExtension.closeAsSuccess().also {
                        Timber.d("receiveApproveSessionAuthenticate: finish; session: ${sessionAuthenticateResponse.session}")
                    }
                }
            }
        }
        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun sendSessionRequestOverAuthenticatedSession() {
        Timber.d("sendSessionRequestOverAuthenticatedSession: start")

        val (privateKey, address) = Pair("fc38e74680851b8d0c2dc69ccd367d4c0d963a4065dff56a87f450eef33336c4", "0xF983704E5A9eF14C32e8fe751b34E61702437aBF")

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionAuthenticate(sessionAuthenticate: Sign.Model.SessionAuthenticate, verifyContext: Sign.Model.VerifyContext) {
                val issuerToMessages = mutableListOf<Pair<String, String>>()
                val cacaos = mutableListOf<Sign.Model.Cacao>()

                sessionAuthenticate.payloadParams.chains.forEach { chain ->
                    val issuer = "did:pkh:$chain:$address"
                    val message = WalletSignClient.formatAuthMessage(Sign.Params.FormatMessage(sessionAuthenticate.payloadParams, issuer)) ?: throw Exception("Invalid message")
                    issuerToMessages.add(issuer to message)
                }

                issuerToMessages.forEach { issuerToMessage ->
                    val messageToSign = Numeric.toHexString(issuerToMessage.second.toByteArray())
                    val signature = CacaoSigner.signHex(messageToSign, privateKey.hexToBytes(), SignatureType.EIP191)
                    val cacao = generateCACAO(sessionAuthenticate.payloadParams, issuerToMessage.first, signature)
                    cacaos.add(cacao)
                }

                val params = Sign.Params.ApproveSessionAuthenticate(sessionAuthenticate.id, cacaos)
                WalletSignClient.approveSessionAuthenticated(params, onSuccess = {}, onError = ::globalOnError)
            }

            override fun onSessionRequest(sessionRequest: Sign.Model.SessionRequest, verifyContext: Sign.Model.VerifyContext) {
                Timber.d("Wallet receives session request: ${sessionRequest.request}")
                walletClientRespondToRequest(sessionRequest.topic, Sign.Model.JsonRpcResponse.JsonRpcResult(sessionRequest.request.id, "dummy"))
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Sign.Model.SessionAuthenticateResponse) {
                if (sessionAuthenticateResponse is Sign.Model.SessionAuthenticateResponse.Result) {
                    Timber.d("Dapp is sending session request")
                    dappClientSendRequest(sessionAuthenticateResponse.session.topic)
                }
            }

            override fun onSessionRequestResponse(response: Sign.Model.SessionRequestResponse) {
                if (response.result is Sign.Model.JsonRpcResponse.JsonRpcResult) {
                    scenarioExtension.closeAsSuccess().also {
                        Timber.d("receiveSessionRequestResponse: finish, ${response}")
                    }
                }
            }
        }

        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun approveSessionAuthenticatedWithInvalidCACAOs() {
        Timber.d("approveSessionAuthenticatedWithInvalidCACAOs: start")

        val (privateKey, address) = Pair("fc38e74680851b8d0c2dc69ccd367d4c0d963a4065dff56a87f450eef33336c4", "0xF983704E5A9eF14C32e8fe751b34E61702437aBF")
        val walletDelegate = object : WalletDelegate() {
            override fun onSessionAuthenticate(sessionAuthenticate: Sign.Model.SessionAuthenticate, verifyContext: Sign.Model.VerifyContext) {
                val messages = mutableListOf<Pair<String, String>>()
                val cacaos = mutableListOf<Sign.Model.Cacao>()

                sessionAuthenticate.payloadParams.chains.forEach { chain ->
                    val issuer = "did:pkh:$chain:$address"
                    val message = WalletSignClient.formatAuthMessage(Sign.Params.FormatMessage(sessionAuthenticate.payloadParams, issuer)) ?: throw Exception("Invalid message")
                    messages.add(issuer to message)
                }

                messages.forEach { message ->
                    val signature = CacaoSigner.signHex("messageToSign", privateKey.hexToBytes(), SignatureType.EIP191)
                    val cacao = generateCACAO(sessionAuthenticate.payloadParams, message.first, signature)
                    cacaos.add(cacao)
                }

                val params = Sign.Params.ApproveSessionAuthenticate(sessionAuthenticate.id, cacaos)
                WalletSignClient.approveSessionAuthenticated(params, onSuccess = {}, onError = {
                    Timber.d("approveSessionAuthenticated: onError: $it")
                })
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Sign.Model.SessionAuthenticateResponse) {
                if (sessionAuthenticateResponse is Sign.Model.SessionAuthenticateResponse.Error) {
                    (sessionAuthenticateResponse.message == "Invalid CACAO").also {
                        Timber.d("receiveApproveSessionAuthenticate: ${sessionAuthenticateResponse.message}: finish")
                        scenarioExtension.closeAsSuccess()
                    }
                }
            }
        }
        launch(walletDelegate, dappDelegate)
    }

    @Test
    fun rejectSessionAuthenticated() {
        Timber.d("rejectSessionAuthenticated: start")

        val walletDelegate = object : WalletDelegate() {
            override fun onSessionAuthenticate(sessionAuthenticate: Sign.Model.SessionAuthenticate, verifyContext: Sign.Model.VerifyContext) {
                val params = Sign.Params.RejectSessionAuthenticate(sessionAuthenticate.id, "User rejections")
                WalletSignClient.rejectSessionAuthenticated(params, onSuccess = {}, onError = ::globalOnError)
            }
        }

        val dappDelegate = object : DappDelegate() {
            override fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Sign.Model.SessionAuthenticateResponse) {
                if (sessionAuthenticateResponse is Sign.Model.SessionAuthenticateResponse.Error) {
                    scenarioExtension.closeAsSuccess().also { Timber.d("receiveRejectSessionAuthenticate: finish") }
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