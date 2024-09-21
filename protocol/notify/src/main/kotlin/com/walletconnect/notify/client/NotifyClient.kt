package com.walletconnect.notify.client

@Deprecated("com.walletconnect.notify.client.NotifyClient has been deprecated. Please use com.reown.notify.client.NotifyClient instead from - https://github.com/reown-com/reown-kotlin")
object NotifyClient: NotifyInterface by NotifyProtocol.instance {
    interface Delegate: NotifyInterface.Delegate
}