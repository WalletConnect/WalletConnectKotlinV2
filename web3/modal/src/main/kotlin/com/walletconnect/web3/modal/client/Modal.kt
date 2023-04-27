package com.walletconnect.web3.modal.client

import com.walletconnect.android.CoreClient

object Modal {
    sealed class Params {
        data class Init(
            val core: CoreClient
        ) : Params()
    }

    sealed class Model {
        data class Error(val throwable: Throwable) : Model()
    }
}