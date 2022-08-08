package com.walletconnect.auth.client

object AuthClient: AuthInterface by AuthProtocol.instance {
    interface AuthDelegate: AuthInterface.AuthDelegate
}