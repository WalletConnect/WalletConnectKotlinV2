package com.walletconnect.auth.client

object AuthClient : AuthInterface by AuthProtocol.instance {
    interface RequesterDelegate : AuthInterface.RequesterDelegate
    interface ResponderDelegate : AuthInterface.ResponderDelegate
}