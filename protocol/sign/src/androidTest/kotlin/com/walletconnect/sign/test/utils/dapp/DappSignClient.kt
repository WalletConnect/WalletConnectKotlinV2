package com.walletconnect.sign.test.utils.dapp

import com.walletconnect.android.Core
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.test.utils.TestClient
import com.walletconnect.sign.test.utils.globalOnError
import com.walletconnect.sign.test.utils.proposalNamespaces
import com.walletconnect.sign.test.utils.sessionChains
import com.walletconnect.sign.test.utils.sessionMethods
import com.walletconnect.util.bytesToHex
import com.walletconnect.util.randomBytes
import timber.log.Timber

val DappSignClient = TestClient.Dapp.signClient

val dappClientConnect = { pairing: Core.Model.Pairing ->
    val connectParams = Sign.Params.Connect(namespaces = proposalNamespaces, optionalNamespaces = null, properties = null, pairing = pairing)
    DappSignClient.connect(
        connectParams,
        onSuccess = { url -> Timber.d("DappClient: connect onSuccess, url: $url") },
        onError = ::globalOnError
    )
}

fun dappClientAuthenticate(onPairing: (String) -> Unit) {
    val authenticateParams = Sign.Params.Authenticate(
        type = "caip222",
        chains = listOf("eip155:1", "eip155:137"),
        domain = "sample.dapp",
        aud = "https://react-auth-dapp.vercel.app/",
        nonce = randomBytes(12).bytesToHex(),
        exp = null,
        nbf = null,
        statement = "Sign in with wallet.",
        requestId = null,
        resources = null,
        methods = listOf("personal_sign", "eth_signTypedData_v4", "eth_sign"),
    )
    DappSignClient.sessionAuthenticate(
        authenticateParams,
        onSuccess = { pairingUrl ->
            Timber.d("DappClient: on sent authenticate success: $pairingUrl")
            onPairing(pairingUrl)
        },
        onError = ::globalOnError
    )
}

val dappClientSendRequest = { topic: String ->
    DappSignClient.request(
        Sign.Params.Request(topic, sessionMethods.first(), "[\"dummy\"]", sessionChains.first()),
        onSuccess = { _: Sign.Model.SentRequest -> Timber.d("Dapp: requestOnSuccess") },
        onError = ::globalOnError
    )
}


