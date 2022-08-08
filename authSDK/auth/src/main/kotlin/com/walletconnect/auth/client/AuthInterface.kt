package com.walletconnect.auth.client

interface AuthInterface {

    interface AuthDelegate {
    }

    fun setAuthDelegate(delegate: AuthDelegate)

    fun initialize(init: Auth.Params.Init, onError: (Auth.Model.Error) -> Unit)
}