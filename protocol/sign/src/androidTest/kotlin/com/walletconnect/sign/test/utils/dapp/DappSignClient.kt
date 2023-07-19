package com.walletconnect.sign.test.utils.dapp

import com.walletconnect.android.Core
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.test.utils.TestClient
import com.walletconnect.sign.test.utils.globalOnError
import com.walletconnect.sign.test.utils.proposalNamespaces
import com.walletconnect.sign.test.utils.sessionChains
import com.walletconnect.sign.test.utils.sessionMethods
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

val dappClientSendRequest = { topic: String ->
    DappSignClient.request(
        Sign.Params.Request(topic, sessionMethods.first(), "[\"dummy\"]", sessionChains.first()),
        onSuccess = { _: Sign.Model.SentRequest -> Timber.d("Dapp: requestOnSuccess") },
        onError = ::globalOnError
    )
}


