package com.walletconnect.android

@Deprecated("com.walletconnect.android.CoreClient has been deprecated. Please use com.reown.android.CoreClient instead from - https://github.com/reown-com/reown-kotlin")
object CoreClient : CoreInterface by CoreProtocol.instance {

    @Deprecated("com.walletconnect.android.CoreDelegate has been deprecated. Please use com.reown.android.CoreDelegate instead from - https://github.com/reown-com/reown-kotlin")
    interface CoreDelegate : CoreInterface.Delegate
}