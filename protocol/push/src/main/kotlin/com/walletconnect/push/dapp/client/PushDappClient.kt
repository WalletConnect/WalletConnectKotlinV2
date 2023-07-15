package com.walletconnect.push.dapp.client

object PushDappClient: PushDappInterface by PushDappProtocol.instance {
    interface Delegate: PushDappInterface.Delegate
}