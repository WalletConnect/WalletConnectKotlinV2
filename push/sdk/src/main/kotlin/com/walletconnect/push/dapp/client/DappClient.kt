package com.walletconnect.push.dapp.client

object DappClient: DappInterface by DappProtocol.instance {
    interface Delegate: DappInterface.Delegate
}