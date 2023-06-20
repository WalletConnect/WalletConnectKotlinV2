package com.walletconnect.android

object CoreClient : CoreInterface<CoreClient.CoreDelegate> by CoreProtocol.instance {

    interface CoreDelegate : CoreInterface.CoreDelegate
}