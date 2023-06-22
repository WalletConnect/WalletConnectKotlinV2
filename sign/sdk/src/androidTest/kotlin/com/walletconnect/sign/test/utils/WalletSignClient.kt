package com.walletconnect.sign.test.utils

import com.walletconnect.sign.client.Sign
import timber.log.Timber

val WalletSignClient = TestClient.Wallet.signClient

val respondToRequest = { topic: String, response: Sign.Model.JsonRpcResponse ->
    WalletSignClient.respond(
        Sign.Params.Response(topic, jsonRpcResponse = response),
        onSuccess = { Timber.d("Wallet: respondOnSuccess") },
        onError = ::globalOnError
    )
}
