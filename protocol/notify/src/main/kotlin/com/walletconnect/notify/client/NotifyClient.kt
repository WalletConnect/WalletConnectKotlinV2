package com.walletconnect.notify.client

object NotifyClient: NotifyInterface by NotifyProtocol.instance {
    interface Delegate: NotifyInterface.Delegate
}