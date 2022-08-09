package com.walletconnect.auth.client

import android.app.Application

object Auth {

    sealed interface Listeners {

    }

    sealed class Model {
        data class Error(val throwable: Throwable) : Model() // TODO: Should this be extracted to core for easier error handling?

        sealed class Events : Model() {}
    }

    sealed class Params {
        data class Init(val application: Application) : Params()
    }
}