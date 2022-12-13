package com.walletconnect.auth.client

object AuthClient: AuthInterface by AuthProtocol.instance {
    interface RequesterDelegate: AuthInterface.RequesterDelegate //todo: change name to WalletDelegate
    interface ResponderDelegate: AuthInterface.ResponderDelegate //todo: change name to DappDelegate
}