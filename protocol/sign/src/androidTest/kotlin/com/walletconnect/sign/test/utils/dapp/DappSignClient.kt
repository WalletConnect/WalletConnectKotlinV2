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
        onSuccess = { Timber.d("DappClient: connect onSuccess") },
        onError = ::globalOnError
    )
}

val dappClientAuthenticate = { pairing: Core.Model.Pairing ->
    val authenticateParams = Sign.Params.Authenticate(
        type = "caip222",
        chains = listOf("eip155:1", "eip155:37"),
        domain = "sample.dapp",
        aud = "https://react-auth-dapp.vercel.app/",
        nonce = randomBytes(12).bytesToHex(),
        iat = "2023-12-14T14:15:55.440Z",
        exp = null,
        nbf = null,
        statement = "Sign in with wallet.",
        requestId = null,
        resources = null,
        pairingTopic = pairing.topic,
        methods = listOf("personal_sign", "eth_signTypedData_v4", "eth_sign"),
    )
    DappSignClient.sessionAuthenticate(
        authenticateParams,
        onSuccess = { Timber.d("DappClient: on sent authenticate success") },
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


